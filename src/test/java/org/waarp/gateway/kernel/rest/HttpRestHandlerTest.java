/**
 * This file is part of Waarp Project.
 * <p>
 * Copyright 2009, Frederic Bregier, and individual contributors by the @author tags. See the COPYRIGHT.txt in the
 * distribution for a full listing of individual contributors.
 * <p>
 * All Waarp Project is free software: you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * Waarp is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with Waarp . If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.waarp.gateway.kernel.rest;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.waarp.common.logging.WaarpLogger;
import org.waarp.common.logging.WaarpLoggerFactory;
import org.waarp.common.utility.WaarpNettyUtil;
import org.waarp.gateway.kernel.exception.HttpInvalidAuthenticationException;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * @author "Frederic Bregier"
 *
 */
public class HttpRestHandlerTest extends HttpRestHandler {
    private static final WaarpLogger logger = WaarpLoggerFactory.getLogger(HttpRestHandlerTest.class);

    public HttpRestHandlerTest(RestConfiguration config) {
        super(config);
        restHashMap = config.restHashMap;
    }

    /**
     * To be called once to ensure default is built
     */
    public static void defaultHandlers() {
        synchronized (defaultConfiguration) {
            if (defaultConfiguration.restHashMap.isEmpty()) {
                defaultConfiguration.REST_AUTHENTICATED = false;
                defaultConfiguration.RESTHANDLERS_CRUD = new byte[RESTHANDLERS.values().length];
                for (int i = 0; i < defaultConfiguration.RESTHANDLERS_CRUD.length; i++) {
                    defaultConfiguration.RESTHANDLERS_CRUD[i] = 0x0F;
                }
                METHOD[] methods = METHOD.values();
                defaultConfiguration.restHashMap.put(RESTHANDLERS.DbTransferLog.uri,
                                                     new DbTransferLogDataModelRestMethodHandler(defaultConfiguration,
                                                                                                 methods));
            }
        }
    }

    protected static METHOD[] getMethods(byte check) {
        List<METHOD> methods = new ArrayList<METHOD>();
        if (RestConfiguration.CRUD.CREATE.isValid(check)) {
            methods.add(METHOD.POST);
        }
        if (RestConfiguration.CRUD.READ.isValid(check)) {
            methods.add(METHOD.GET);
        }
        if (RestConfiguration.CRUD.UPDATE.isValid(check)) {
            methods.add(METHOD.PUT);
        }
        if (RestConfiguration.CRUD.DELETE.isValid(check)) {
            methods.add(METHOD.DELETE);
        }
        return methods.toArray(new METHOD[0]);
    }

    public static void instantiateHandlers(RestConfiguration restConfiguration) {
        defaultHandlers();
        byte check = restConfiguration.RESTHANDLERS_CRUD[RESTHANDLERS.DbTransferLog.ordinal()];
        if (check != 0) {
            METHOD[] methods = getMethods(check);
            restConfiguration.restHashMap.put(RESTHANDLERS.DbTransferLog.uri,
                                              new DbTransferLogDataModelRestMethodHandler(restConfiguration, methods));
        }
        logger.debug("Initialized handler: " + RESTHANDLERS.values().length);
    }

    /**
     * Initialize the REST service (server side) for one restConfiguration
     *
     * @param restConfiguration
     */
    public static void initializeService(RestConfiguration restConfiguration) {
        instantiateHandlers(restConfiguration);
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        // Configure the server.
        ServerBootstrap httpBootstrap = new ServerBootstrap();
        WaarpNettyUtil.setServerBootstrap(httpBootstrap, bossGroup, workerGroup, 30000);

        // Configure the pipeline factory.
        httpBootstrap.childHandler(new HttpRestInitializer(restConfiguration));
        // Bind and start to accept incoming connections.
        ChannelFuture future = null;
        if (restConfiguration != null && !restConfiguration.REST_ADDRESS.isEmpty()) {
            future = httpBootstrap.bind(new InetSocketAddress(restConfiguration.REST_ADDRESS,
                                                              restConfiguration.REST_PORT));
        } else {
            future = httpBootstrap.bind(new InetSocketAddress(restConfiguration.REST_PORT));
        }
        try {
            future.await();
            group.add(future.channel());
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void checkConnection(ChannelHandlerContext ctx) throws HttpInvalidAuthenticationException {
        arguments.methodFromUri();
        arguments.methodFromHeader();
    }

    public static enum RESTHANDLERS {
        DbTransferLog(DbTransferLogDataModelRestMethodHandler.BASEURI,
                      org.waarp.gateway.kernel.database.data.DbTransferLog.class);

        public String uri;
        @SuppressWarnings("rawtypes")
        public Class clasz;

        @SuppressWarnings("rawtypes")
        RESTHANDLERS(String uri, Class clasz) {
            this.uri = uri;
            this.clasz = clasz;
        }

        public static RESTHANDLERS getRESTHANDLER(String baseUri) {
            for (RESTHANDLERS resthandler : RESTHANDLERS.values()) {
                if (resthandler.uri.equals(baseUri)) {
                    return resthandler;
                }
            }
            return null;
        }
    }

}
