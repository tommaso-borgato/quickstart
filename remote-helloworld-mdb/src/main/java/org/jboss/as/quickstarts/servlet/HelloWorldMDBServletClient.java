/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.quickstarts.servlet;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.jms.Destination;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSDestinationDefinition;
import jakarta.jms.JMSDestinationDefinitions;
import jakarta.jms.Queue;
import jakarta.jms.Topic;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

/**
 * Definition of the two JMS destinations used by the quickstart
 * (one queue and one topic).
 */
@JMSDestinationDefinitions(
    value = {
        @JMSDestinationDefinition(
            name = "java:/queue/HELLOWORLDMDBQueue",
            interfaceName = "jakarta.jms.Queue",
            destinationName = "HelloWorldMDBQueue",
            properties = {"enable-amq1-prefix=false"} // if you remove this, a useless jms.queue.HelloWorldMDBQueue is created
        ),
        @JMSDestinationDefinition(
            name = "java:/topic/HELLOWORLDMDBTopic",
            interfaceName = "jakarta.jms.Topic",
            destinationName = "HelloWorldMDBTopic",
            properties = {"enable-amq1-prefix=false"}
        ),
        @JMSDestinationDefinition(
                name = "java:/queue/SomeQueue1",
                interfaceName = "jakarta.jms.Queue",
                destinationName = "SomeQueue1",
                properties = {"enable-amq1-prefix=false"}
        ),
        @JMSDestinationDefinition(
                name = "java:/queue/SomeQueue2",
                interfaceName = "jakarta.jms.Queue",
                destinationName = "SomeQueue2",
                properties = {"enable-amq1-prefix=true"}
        ),
        @JMSDestinationDefinition(
                name = "java:/queue/SomeQueue3",
                interfaceName = "jakarta.jms.Queue",
                destinationName = "SomeQueue3"
        )
    }
)

/**
 * A simple servlet as client that sends several messages to a queue or a topic.
 * @author Emmanuel Hugonnet (c) 2023 Red Hat, Inc.
 */
@WebServlet("/HelloWorldMDBServletClient")
public class HelloWorldMDBServletClient extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(HelloWorldMDBServletClient.class.toString());

    private static final long serialVersionUID = -8314035702649252239L;

    private static final int MSG_COUNT = 5;

    @Inject
    private transient JMSContext context;

    @Resource(lookup = "java:/queue/HELLOWORLDMDBQueue")
    private transient Queue queue;

    @Resource(lookup = "java:/topic/HELLOWORLDMDBTopic")
    private transient Topic topic;

    @Resource(lookup = "java:/queue/SomeQueue1")
    private transient Queue queue1;

    @Resource(lookup = "java:/queue/SomeQueue2")
    private transient Queue queue2;

    @Resource(lookup = "java:/queue/SomeQueue3")
    private transient Queue queue3;

    // /subsystem=messaging-activemq/external-jms-queue=myExternalQueue:add(entries=[java:jboss/exported/jms/queue/myExternalQueue])
    // useless myExternalQueue destination is created, messages go to jms.queue.myExternalQueue
    @Resource(lookup = "java:jboss/exported/jms/queue/myExternalQueue")
    private transient Queue queue4;

    // /subsystem=messaging-activemq/external-jms-queue=myExternalQueueTrue:add(entries=[java:jboss/exported/jms/queue/myExternalQueueTrue], enable-amq1-prefix=true)
    // no useless destination is created, messages go to jms.queue.myExternalQueueTrue
    @Resource(lookup = "java:jboss/exported/jms/queue/myExternalQueueTrue")
    private transient Queue queue5;

    // /subsystem=messaging-activemq/external-jms-queue=myExternalQueueFalse:add(entries=[java:jboss/exported/jms/queue/myExternalQueueFalse], enable-amq1-prefix=false)
    // no useless destination is created, messages go to myExternalQueueFalse
    @Resource(lookup = "java:jboss/exported/jms/queue/myExternalQueueFalse")
    private transient Queue queue6;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        try (PrintWriter out = resp.getWriter()) {
            out.println("<h1>Quickstart: Example demonstrates the use of <strong>Jakarta Messaging 3.1</strong> and <strong>Jakarta Enterprise Beans 4.0 Message-Driven Bean</strong> in a JakartaEE server.</h1>");
            boolean useTopic = req.getParameterMap().keySet().contains("topic");
            final Destination destination = useTopic ? topic : queue;

            try {
            out.println("<p>Sending messages to <em>" + destination + "</em></p>");
            out.println("<h2>The following messages will be sent to the destination:</h2>");
            for (int i = 0; i < MSG_COUNT; i++) {
                String text = "This is message " + (i + 1);
                context.createProducer().send(destination, text);
                out.println("Message (" + i + "): " + text + "<br/>");
            }
            } catch (Exception log) {
                LOGGER.severe("[" + queue + "] " + log.getMessage());
                out.println("<h2>" + queue + "</h2><p>");
                log.printStackTrace(out);
                out.println("</p>");
            }

            try {
                out.println("<p>Sending messages to <em>" + queue1 + "</em></p>");
                out.println("<h2>The following messages will be sent to the SomeQueue1 destination:</h2>");
                for (int i = 0; i < MSG_COUNT; i++) {
                    String text = "This is message " + (i + 1);
                    context.createProducer().send(queue1, text);
                    out.println("Message (" + i + "): " + text + "<br/>");
                }
            } catch (Exception log) {
                LOGGER.severe("[" + queue1 + "] " + log.getMessage());
                out.println("<h2>" + queue1 + "</h2><p>");
                log.printStackTrace(out);
                out.println("</p>");
            }

            try {
                out.println("<p>Sending messages to <em>" + queue2 + "</em></p>");
                out.println("<h2>The following messages will be sent to the SomeQueue2 destination:</h2>");
                for (int i = 0; i < MSG_COUNT; i++) {
                    String text = "This is message " + (i + 1);
                    context.createProducer().send(queue2, text);
                    out.println("Message (" + i + "): " + text + "<br/>");
                }
            } catch (Exception log) {
                LOGGER.severe("[" + queue2 + "] " + log.getMessage());
                out.println("<h2>" + queue2 + "</h2><p>");
                log.printStackTrace(out);
                out.println("</p>");
            }

            try {
                out.println("<p>Sending messages to <em>" + queue3 + "</em></p>");
                out.println("<h2>The following messages will be sent to the SomeQueue3 destination:</h2>");
                for (int i = 0; i < MSG_COUNT; i++) {
                    String text = "This is message " + (i + 1);
                    context.createProducer().send(queue3, text);
                    out.println("Message (" + i + "): " + text + "<br/>");
                }
            } catch (Exception log) {
                LOGGER.severe("[" + queue3 + "] " + log.getMessage());
                out.println("<h2>" + queue3 + "</h2><p>");
                log.printStackTrace(out);
                out.println("</p>");
            }

            try {
                out.println("<p>Sending messages to <em>" + queue4 + "</em></p>");
                out.println("<h2>The following messages will be sent to the myExternalQueue destination:</h2>");
                for (int i = 0; i < MSG_COUNT; i++) {
                    String text = "This is message " + (i + 1);
                    context.createProducer().send(queue4, text);
                    out.println("Message (" + i + "): " + text + "<br/>");
                }
            } catch (Exception log) {
                LOGGER.severe("[" + queue4 + "] " + log.getMessage());
                out.println("<h2>" + queue4 + "</h2><p>");
                log.printStackTrace(out);
                out.println("</p>");
            }

            try {
                out.println("<p>Sending messages to <em>" + queue5 + "</em></p>");
                out.println("<h2>The following messages will be sent to the myExternalQueueTrue destination:</h2>");
                for (int i = 0; i < MSG_COUNT; i++) {
                    String text = "This is message " + (i + 1);
                    context.createProducer().send(queue5, text);
                    out.println("Message (" + i + "): " + text + "<br/>");
                }
            } catch (Exception log) {
                LOGGER.severe("[" + queue5 + "] " + log.getMessage());
                out.println("<h2>" + queue5 + "</h2><p>");
                log.printStackTrace(out);
                out.println("</p>");
            }

            try {
                out.println("<p>Sending messages to <em>" + queue6 + "</em></p>");
                out.println("<h2>The following messages will be sent to the myExternalQueueFalse destination:</h2>");
                for (int i = 0; i < MSG_COUNT; i++) {
                    String text = "This is message " + (i + 1);
                    context.createProducer().send(queue6, text);
                    out.println("Message (" + i + "): " + text + "<br/>");
                }
            } catch (Exception log) {
                LOGGER.severe("[" + queue6 + "] " + log.getMessage());
                out.println("<h2>" + queue6 + "</h2><p>");
                log.printStackTrace(out);
                out.println("</p>");
            }

            out.println("<p><i>Go to your JakartaEE server console or server log to see the result of messages processing.</i></p>");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
