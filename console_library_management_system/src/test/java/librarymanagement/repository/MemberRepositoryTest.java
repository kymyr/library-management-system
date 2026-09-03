package librarymanagement.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import librarymanagement.model.Member;
import org.junit.jupiter.api.Test;

class MemberRepositoryTest {
    @Test
    void repositoryRegistersAndFindsMember() {
        MemberRepository memberRepository = new MemberRepository();
        Member member = new Member(101, "Test Member");

        assertTrue(memberRepository.registerMember(member));
        assertEquals(member, memberRepository.findById(101));
        assertEquals(1, memberRepository.getAllMembers().size());
    }

    @Test
    void repositoryRejectsDuplicateMemberIds() {
        MemberRepository memberRepository = new MemberRepository();

        assertTrue(memberRepository.registerMember(new Member(102, "First Member")));
        assertFalse(memberRepository.registerMember(new Member(102, "Duplicate Member")));
        assertEquals(1, memberRepository.getAllMembers().size());
    }

    @Test
    void repositoryReturnsNullForMissingMember() {
        MemberRepository memberRepository = new MemberRepository();

        assertNull(memberRepository.findById(999));
    }
}