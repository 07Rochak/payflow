package com.rochak.payflow.repository;

import com.rochak.payflow.session.UserSession;
import org.springframework.data.repository.CrudRepository;

public interface UserSessionRepository extends CrudRepository<UserSession, String> {
}
