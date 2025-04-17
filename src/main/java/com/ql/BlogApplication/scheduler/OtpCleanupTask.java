package com.ql.BlogApplication.scheduler;

import com.ql.BlogApplication.repository.OtpRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;

@Component
public class OtpCleanupTask {

    private final OtpRepository otpRepository;
    Logger logger= LoggerFactory.getLogger(OtpCleanupTask.class);

    @Value("${otp.cleanup.interval}")
    private Long cleanupInterval;

    OtpCleanupTask(OtpRepository otpRepository){
        this.otpRepository=otpRepository;
    }

    @Transactional
    @Scheduled(fixedRateString ="${otp.cleanup.fixed-rate}" )
    public void deleteExpiredToken(){

        try{
            LocalDateTime cutoff= LocalDateTime.now().minusMinutes(cleanupInterval);
            logger.info("Cleaning up OTPs older than: {}", cutoff);
            otpRepository.deleteByGeneratedAtBefore(cutoff);
            logger.info("Expired OTPs successfully cleaned up.");
        }catch (Exception e){
            logger.error("Error occurred while cleaning up expired OTPs", e);
        }
    }


}
