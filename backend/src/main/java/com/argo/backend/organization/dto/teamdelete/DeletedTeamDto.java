package com.argo.backend.organization.dto.teamdelete;

import com.argo.backend.domain.classroom.entity.ClassRoom;
import com.argo.backend.domain.team.entity.Team;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class DeletedTeamDto {
    
    private Long teamId;
    private String teamName;
    private Long classId;
    private String className;
    private LocalDateTime deletedAt;
    
    public DeletedTeamDto(Long teamId, String teamName, Long classId, String className, LocalDateTime deletedAt) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.classId = classId;
        this.className = className;
        this.deletedAt = deletedAt;
    }
    
    public static DeletedTeamDto from(Team team, LocalDateTime deletedAt) {
        ClassRoom classRoom = team.getClassRoom();
        return new DeletedTeamDto(
                team.getTeamId(),
                team.getTeamName(),
                classRoom.getClassId(),
                classRoom.getClassName(),
                deletedAt
        );
    }
}