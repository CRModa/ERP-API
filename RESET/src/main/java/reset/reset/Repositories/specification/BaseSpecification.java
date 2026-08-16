package reset.reset.Repositories.specification;

import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static reset.reset.Repositories.specification.FilterOperation.*;

public abstract class BaseSpecification<T> implements Specification<T> {

    protected final List<FilterCriteria> criteria = new ArrayList<>();

    public void addFilter(String key, Object value, FilterOperation operation) {
        if (value != null && !value.toString().isEmpty()) {
            criteria.add(new FilterCriteria(key, value, operation));
        }
    }

    public void addDateRange(String key, LocalDate startDate, LocalDate endDate) {
        if (startDate != null) {
            addFilter(key, startDate, FilterOperation.GREATER_THAN_OR_EQUAL);
        }
        if (endDate != null) {
            addFilter(key, endDate, FilterOperation.LESS_THAN_OR_EQUAL);
        }
    }

    public void addDateTimeRange(String key, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime != null) {
            addFilter(key, startDateTime, FilterOperation.GREATER_THAN_OR_EQUAL);
        }
        if (endDateTime != null) {
            addFilter(key, endDateTime, FilterOperation.LESS_THAN_OR_EQUAL);
        }
    }

    public void addBetween(String key, Object start, Object end) {
        if (start != null && end != null) {
            criteria.add(new FilterCriteria(key, new Object[]{start, end}, FilterOperation.BETWEEN));
        } else if (start != null) {
            addFilter(key, start, FilterOperation.GREATER_THAN_OR_EQUAL);
        } else if (end != null) {
            addFilter(key, end, FilterOperation.LESS_THAN_OR_EQUAL);
        }
    }

    public void addIn(String key, List<?> values) {
        if (values != null && !values.isEmpty()) {
            criteria.add(new FilterCriteria(key, values, FilterOperation.IN));
        }
    }

    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        for (FilterCriteria filter : criteria) {
            if (filter.getKey().contains(".")) {
                String[] path = filter.getKey().split("\\.");
                Path<Object> pathObject = (Path<Object>) root;
                for (String p : path) {
                    pathObject = pathObject.get(p);
                }
                predicates.add(buildPredicate(pathObject, filter, cb));
            } else {
                predicates.add(buildPredicate(root.get(filter.getKey()), filter, cb));
            }
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }



    private Predicate buildPredicate(Path<?> path, FilterCriteria filter, CriteriaBuilder cb) {
        Object value = filter.getValue();

        switch (filter.getOperation()) {
            case EQUALS:
                return cb.equal(path, value);

            case NOT_EQUALS:
                return cb.notEqual(path, value);

            case LIKE:
            case LIKE_START:
            case LIKE_END:
                return buildLikePredicate(path, filter, cb);

            case GREATER_THAN:
                return cb.greaterThan((Path<Comparable>) path, (Comparable) value);

            case GREATER_THAN_OR_EQUAL:
                return cb.greaterThanOrEqualTo((Path<Comparable>) path, (Comparable) value);

            case LESS_THAN:
                return cb.lessThan((Path<Comparable>) path, (Comparable) value);

            case LESS_THAN_OR_EQUAL:
                return cb.lessThanOrEqualTo((Path<Comparable>) path, (Comparable) value);

            case BETWEEN:
                Object[] values = (Object[]) value;
                return cb.between(
                        (Path<Comparable>) path,
                        (Comparable) values[0],
                        (Comparable) values[1]
                );

            case IN:
                return path.in((List<?>) value);

            case IS_NULL:
                return cb.isNull(path);

            case IS_NOT_NULL:
                return cb.isNotNull(path);

            case TRUE:
                return cb.isTrue(path.as(Boolean.class));

            case FALSE:
                return cb.isFalse(path.as(Boolean.class));

            default:
                throw new IllegalArgumentException("Unsupported operation: " + filter.getOperation());
        }
    }

    private Predicate buildLikePredicate(Path<?> path, FilterCriteria filter, CriteriaBuilder cb) {
        String value = filter.getValue().toString().toLowerCase();
        Path<String> stringPath = (Path<String>) path.as(String.class);

        switch (filter.getOperation()) {
            case LIKE:
                return cb.like(cb.lower(stringPath), "%" + value + "%");
            case LIKE_START:
                return cb.like(cb.lower(stringPath), value + "%");
            case LIKE_END:
                return cb.like(cb.lower(stringPath), "%" + value);
            default:
                throw new IllegalArgumentException("Invalid LIKE operation");
        }
    }
}

