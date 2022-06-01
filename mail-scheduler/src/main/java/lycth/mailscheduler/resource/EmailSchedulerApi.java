package lycth.mailscheduler.resource;

import lombok.extern.slf4j.Slf4j;
import lycth.mailscheduler.dto.EmailRequestDto;
import lycth.mailscheduler.dto.EmailResponseDto;
import lycth.mailscheduler.quartzJob.EmailJob;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

@Slf4j
@RestController

public class EmailSchedulerApi {

    private static final Logger logger = LoggerFactory.getLogger(EmailSchedulerApi.class);
    @Autowired
    private Scheduler scheduler;

    @GetMapping("/get")
    public ResponseEntity<String> getApiTest(){
        return ResponseEntity.ok("GET API TEST- PASS");
    }

    @PostMapping("/scheduler/mail")
    public ResponseEntity<EmailResponseDto> scheduleMail(@Valid @RequestBody EmailRequestDto emailRequestDto){
        try {
            ZonedDateTime dateTime = ZonedDateTime.of(emailRequestDto.getDateTime(), emailRequestDto.getTimeZone());
            if (dateTime.isBefore(ZonedDateTime.now())){
                EmailResponseDto dto = new EmailResponseDto(false, "date time must be after current time");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);
            }
            JobDetail jobDetail = buildJobDetail(emailRequestDto);
            Trigger trigger = buildTrigger(jobDetail, dateTime);
            scheduler.scheduleJob(jobDetail, trigger);

            EmailResponseDto emailResponse = new EmailResponseDto(true,jobDetail.getKey().getName(),
                    jobDetail.getKey().getGroup() ,"Send mail successfully!");
            return ResponseEntity.ok(emailResponse);

        }catch (SchedulerException e){
            logger.error("Error while scheduling email: ", e);
            EmailResponseDto emailResponseDto = new EmailResponseDto(false,"Error while scheduling email! Try later");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(emailResponseDto);
        }
    }
    private JobDetail buildJobDetail(EmailRequestDto dto){
        JobDataMap jobDataMap = new JobDataMap();

        jobDataMap.put("email", dto.getEmail());
        jobDataMap.put("subject", dto.getSubject());
        jobDataMap.put("body", dto.getBody());

        return JobBuilder.newJob(EmailJob.class).withIdentity(UUID.randomUUID().toString(), "email-jobs").
                withDescription("Send Mail Job").usingJobData(jobDataMap).storeDurably().build();
    }
    private Trigger buildTrigger(JobDetail jobDetail, ZonedDateTime startAt){
        return TriggerBuilder.newTrigger().forJob(jobDetail).
                withIdentity(jobDetail.getKey().getName(), "email-trigger").
                withDescription("Send mail trigger").
                startAt(Date.from(startAt.toInstant())).withSchedule(SimpleScheduleBuilder.
                        simpleSchedule().withMisfireHandlingInstructionFireNow()).build();
    }
}
