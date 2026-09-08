package librarymanagement.service;

import java.io.IOException;
import java.time.LocalDate;

import librarymanagement.model.Member;
import librarymanagement.repository.MemberRepository;

public class MemberService {
    private static final int MEMBERSHIP_YEARS = 1;

    private final MemberRepository memberRepo;
    private final CsvService csvService;

    public MemberService(MemberRepository memberRepo, CsvService csvService) {
        this.memberRepo = memberRepo;
        this.csvService = csvService;
    }

    public Member buildMember(String firstName, String lastName, String email, LocalDate joinDate) {
        return new Member(memberRepo.getNextMemberId(), firstName + " " + lastName, email,
            joinDate.toString(), joinDate.plusYears(MEMBERSHIP_YEARS).toString(), "Active");
    }

    public boolean register(Member member) throws IOException {
        if (!memberRepo.registerMember(member)) {
            return false;
        }
        csvService.saveMembers(memberRepo);
        return true;
    }
}
