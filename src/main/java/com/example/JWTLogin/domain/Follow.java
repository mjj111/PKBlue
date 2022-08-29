package com.example.JWTLogin.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "from_member_id")
    @ManyToOne
    private Member fromMember;

    @JoinColumn(name = "to_member_id")
    @ManyToOne
    private Member toMember;

    private boolean f4f;

    //fromMember와 toMember는 ManyToOne 관계로  N : 1 이다.
    @Builder
    public Follow(Member fromMember, Member toMember, boolean f4f){
        this.fromMember = fromMember;
        this.toMember = toMember;
        this.f4f = f4f;
    }
}
