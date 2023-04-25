package de.witcom.api.service;

import java.time.Duration;
import java.time.Instant;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.core.SimpleLock;

@Log4j2
@Service
@RequiredArgsConstructor
public class LockManager {

	private final LockProvider lockProvider;

	public Optional<SimpleLock> lock(String lockName,Duration minimumLockDuration){

		LockConfiguration lockConfiguration = new LockConfiguration(Instant.now(),
			lockName,
			minimumLockDuration,
			minimumLockDuration
		);
		log.debug(String.format("Trying to lock %s", lockConfiguration.toString()));

		//trying to lock
		return lockProvider.lock(lockConfiguration); 

	}

	public void unlock(Optional<SimpleLock> lock){
		lock.ifPresent(mylock -> mylock.unlock());
	}
	
}
