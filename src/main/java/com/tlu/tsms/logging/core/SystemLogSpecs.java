package com.tlu.tsms.logging.core;

import com.tlu.tsms.common.ESystemLogLevel;
import com.tlu.tsms.entity.SystemLogEntity;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.tlu.tsms.common.Constant.*;

public class SystemLogSpecs {

    public static Specification<SystemLogEntity> filter(
            String q,
            ESystemLogLevel level,
            String module,
            String action,
            String actorName,
            String traceId,
            Date from,
            Date to
    ) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (level != null) ps.add(cb.equal(root.get(LEVEL), level));
            if (module != null && !module.isBlank()) ps.add(cb.equal(root.get(MODULE), module));
            if (action != null && !action.isBlank()) ps.add(cb.equal(root.get(ACTION), action));
            if (actorName != null) ps.add(cb.equal(root.get(ACTOR_NAME), actorName));
            if (traceId != null && !traceId.isBlank()) ps.add(cb.equal(root.get(MDC_TRACE_ID), traceId));
            if (from != null) ps.add(cb.greaterThanOrEqualTo(root.get(TIMESTAMP), from));
            if (to != null) ps.add(cb.lessThanOrEqualTo(root.get(TIMESTAMP), to));
            if (Strings.isNotBlank(q)) {
                String like = "%" + q.trim().toLowerCase() + "%";
                ps.add(cb.or(
                        cb.like(cb.lower(root.get(MESSAGE)), like),
                        cb.like(cb.lower(root.get(ACTOR_NAME)), like),
                        cb.like(cb.lower(root.get(MODULE)), like),
                        cb.like(cb.lower(root.get(ACTION)), like),
                        cb.like(cb.lower(root.get(RESOURCE_TYPE)), like)
                ));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }
}
