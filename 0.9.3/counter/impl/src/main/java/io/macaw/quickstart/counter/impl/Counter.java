//
//  This file was auto-generated by Macaw tools 0.3.0-SNAPSHOT version built on Tue, 14 Feb 2017 14:52:15 +0530 
//
/**
 * Copyright © 2015-2016, Macaw Software Inc.
 * All rights reserved.
 * <p>
 * This software and related documentation are provided under a
 * license agreement containing restrictions on use and
 * disclosure and are protected by intellectual property laws.
 * Except as expressly permitted in your license agreement or
 * allowed by law, you may not use, copy, reproduce, translate,
 * broadcast, modify, license, transmit, distribute, exhibit,
 * perform, publish, or display any part, in any form, or by
 * any means. Reverse engineering, disassembly, or
 * decompilation of this software, unless required by law for
 * interoperability, is prohibited.
 * <p>
 * The information contained herein is subject to change
 * without notice and is not warranted to be error-free. If you
 * find any errors, please report them to us in writing.
 */
package io.macaw.quickstart.counter.impl;

import com.cfx.service.api.config.Configuration;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;


/**
 * A {@link io.macaw.quickstart.counter.Counter} service which uses Cassandra database for storing the counter
 */
public class Counter implements com.cfx.service.api.Service, io.macaw.quickstart.counter.Counter {

    private static final String COLON = ":";
    private static final int DEFAULT_CASSANDRA_PORT = 9042;
    private static final String DB_CASSANDRA_KEYSPACE = "db.cassandra.keyspace";
    private static final String DB_CASSANDRA_PASSWORD = "db.cassandra.password";
    private static final String DB_CASSANDRA_USERNAME = "db.cassandra.username";
    private static final String DB_CASSANDRA_CLUSTER_NODES = "db.cassandra.clusterNodes";

    private Configuration serviceConfig;
    private Cluster dbCluster;
    private String dbKeyspace;

    @Override
    public void initialize(com.cfx.service.api.config.Configuration config) throws com.cfx.service.api.ServiceException {
        this.serviceConfig = config;
        this.dbKeyspace = (String) serviceConfig.requireValue(DB_CASSANDRA_KEYSPACE).getValue();
    }

    @Override
    public void start(com.cfx.service.api.StartContext startContext) throws com.cfx.service.api.ServiceException {
        // create the cassandra DB cluster using the service configs
        this.dbCluster = this.createCluster(this.serviceConfig);
    }

    @Override
    public void stop(com.cfx.service.api.StopContext stopContext) throws com.cfx.service.api.ServiceException {
        // close the db cluster
        if (this.dbCluster != null) {
            this.dbCluster.close();
        }
    }

    @Override
    public long increment() {
        return this.incrementBy(1);
    }

    @Override
    public long incrementBy(final long value) {
        try (final Session dbSession = this.createCassandraSession()) {
            // increment the counter
            final PreparedStatement preparedStatement = dbSession.prepare("update basic_counter set current_val = current_val + ? where name = 'default'");
            final Statement statement = preparedStatement.bind(new Object[]{value});
            dbSession.execute(statement);
            // get latest value of counter
            final ResultSet resultSet = dbSession.execute("select current_val from basic_counter where name = 'default'");
            return resultSet.one().getLong("current_val");
        }
    }

    @Override
    public long getCurrentCount() {
        try (final Session dbSession = this.createCassandraSession()) {
            // get latest value of counter
            final ResultSet resultSet = dbSession.execute("select current_val from basic_counter where name = 'default'");
            return resultSet.one().getLong("current_val");
        }
    }

    private Cluster createCluster(final Configuration configuration) {
        final String nodeAndPorts = (String) configuration.requireValue(DB_CASSANDRA_CLUSTER_NODES).getValue();
        final String[] nodeAndPortsArr = nodeAndPorts.split(",");
        final List<InetSocketAddress> nodeAddresses = new ArrayList<>();
        // parse the node:port combinations into InetSocketAddress
        for (final String nodeAndPort : nodeAndPortsArr) {
            String node = nodeAndPort;
            int port = DEFAULT_CASSANDRA_PORT;
            if (nodeAndPort.indexOf(COLON) > 0) {
                final int indexOfColon = nodeAndPort.indexOf(COLON);
                node = nodeAndPort.substring(0, indexOfColon);
                port = Integer.parseInt(nodeAndPort.substring(indexOfColon + 1));
            }
            InetSocketAddress address = new InetSocketAddress(node, port);
            nodeAddresses.add(address);
        }
        final String username = (String) configuration.requireValue(DB_CASSANDRA_USERNAME).getValue();
        final String password = (String) configuration.requireValue(DB_CASSANDRA_PASSWORD).getValue();
        // build the cluster with the configs
        final Cluster.Builder bldr = Cluster.builder().withProtocolVersion(ProtocolVersion.V3);
        for (final InetSocketAddress node : nodeAddresses) {
            if (node == null) {
                continue;
            }
            bldr.addContactPointsWithPorts(node);
        }
        return bldr.withCredentials(username, password).build();
    }

    private Session createCassandraSession() {
        return this.dbCluster.connect(this.dbKeyspace);
    }
}
