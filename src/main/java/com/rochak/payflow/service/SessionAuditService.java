package com.rochak.payflow.service;

import com.rochak.payflow.dto.SessionAuditReport;

public interface SessionAuditService {
    SessionAuditReport generateAuditReport();
}
