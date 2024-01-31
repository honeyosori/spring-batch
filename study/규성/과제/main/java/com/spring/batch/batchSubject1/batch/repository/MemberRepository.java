package com.spring.batch.batchSubject1.batch.repository;

import com.spring.batch.batchSubject1.batch.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

}
