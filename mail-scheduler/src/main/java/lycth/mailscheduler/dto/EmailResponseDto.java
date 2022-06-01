package lycth.mailscheduler.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailResponseDto {

    private boolean success;

    private String jobId;

    private String jobGroup;

    private String message;

    public EmailResponseDto(boolean success, String jobId, String jobGroup, String message) {
        this.success = success;
        this.jobId = jobId;
        this.jobGroup = jobGroup;
        this.message = message;
    }

    public EmailResponseDto(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
