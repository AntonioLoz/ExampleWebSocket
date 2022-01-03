package com.lozano.examplewebsocket.controller;

import com.lozano.examplewebsocket.dto.Message;
import com.lozano.examplewebsocket.dto.OutputMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;

import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.socket.WebSocketMessage;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Controller
@Slf4j
public class WsController {


    @Autowired
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final Set<String> connectedUsers;

    public WsController(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.connectedUsers = new HashSet<>();
    }


    @MessageMapping("/register")
    @SendToUser("/queue/newMember")
    public Set<String> registerUser(String webChatUsername) {
        log.info("register: {}", webChatUsername);
        if(!this.connectedUsers.contains(webChatUsername)) {
            connectedUsers.add(webChatUsername);
            simpMessagingTemplate.convertAndSend("/topic/newMember", webChatUsername);
            return connectedUsers;
        }
        else {
            return new HashSet<>();
        }
    }

    @MessageMapping("/unregister")
    @SendTo("/topic/disconnectedUser")
    public String unregisterUser(String webChatUsername) {
        log.info("unregister: {}", webChatUsername);
        connectedUsers.remove(webChatUsername);
        return webChatUsername;
    }

    @MessageMapping("/message")
    public void send(WebSocketMessage<Message> msg) throws Exception {
        log.info("message: {}", msg.getPayload());

        OutputMessage out = new OutputMessage(
                msg.getPayload().getFrom(),
                msg.getPayload().getText(),
                new SimpleDateFormat("HH:mm").format(new Date()));
        simpMessagingTemplate.convertAndSendToUser(
                msg.getPayload().getTo(), "/user/queue/specific-user", out);
        System.out.println("SessionId: " + msg.getPayload().toString());

    }
}
