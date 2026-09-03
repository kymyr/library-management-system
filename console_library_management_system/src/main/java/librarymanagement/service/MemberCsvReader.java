package librarymanagement.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import librarymanagement.model.Member;
import librarymanagement.repository.MemberRepository;

public class MemberCsvReader {
    public int load(Path path, MemberRepository memberRepository) throws IOException {
        List<String> lines = Files.readAllLines(path);
        int loadedMembers = 0;

        for (int lineNumber = 1; lineNumber < lines.size(); lineNumber++) {
            String line = lines.get(lineNumber).trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] fields = line.split(",", -1);
            if (fields.length != 7) {
                continue;
            }

            try {
                int memberId = Integer.parseInt(fields[0].trim());
                String name = fields[1].trim() + " " + fields[2].trim();
                Member member = new Member(memberId, name, fields[3].trim(), fields[4].trim(),
                        fields[5].trim(), fields[6].trim());
                if (memberRepository.registerMemberQuietly(member)) {
                    loadedMembers++;
                }
            } catch (NumberFormatException ignored) {
                // Ignore malformed rows while loading the remaining members.
            }
        }

        return loadedMembers;
    }
}