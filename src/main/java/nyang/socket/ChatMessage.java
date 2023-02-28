package nyang.socket;

import lombok.Getter;
import lombok.Setter;
import nyang.cat.Users.entity.Users;
import org.hibernate.annotations.DynamicInsert;

import javax.persistence.*;
import java.lang.reflect.Member;
import java.time.Instant;

@Entity
@DynamicInsert
@Getter
@Setter
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_user_id")
    private Users sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_user_id")
    private Users receiver;

    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private ChatRoom chatRoom;

    public ChatMessage(Users sender, Users receiver, String message, ChatRoom chatRoom) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.chatRoom = chatRoom;
    }

    public ChatMessage() {

    }
}
