package librarymanagement.service;

import java.time.LocalDate;

import librarymanagement.model.Member;
import librarymanagement.repository.MemberRepository;

public class MemberService {
    private static final int MEMBERSHIP_YEARS = 1;

    private final MemberRepository memberRepo;

    public MemberService(MemberRepository memberRepo) {
        this.memberRepo = memberRepo;
    }

    public Member buildMember(String firstName, String lastName, String email, LocalDate joinDate) {
        return new Member(memberRepo.getNextMemberId(), firstName + " " + lastName, email,
                joinDate.toString(), joinDate.plusYears(MEMBERSHIP_YEARS).toString(), "Active");
    }

    public boolean register(Member member) {
        return memberRepo.registerMember(member);
    }
}
