package kr.giljabi.api.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @Author : njpark@hyosung.com
 * @Date : 2024.06.05
 * @Description
 */
@Entity
@Getter
@Setter
@Table(name = "userinfo")
public class UserInfo implements java.io.Serializable {
    @Id
    private int seqno;
    private String userid;
    private String password;
    private String username;
    private String level;
}
