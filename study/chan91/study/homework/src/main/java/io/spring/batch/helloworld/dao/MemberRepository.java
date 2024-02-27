package io.spring.batch.helloworld.dao;

import io.spring.batch.helloworld.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Integer> {
}
