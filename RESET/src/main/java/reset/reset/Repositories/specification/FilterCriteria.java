package reset.reset.Repositories.specification;

import lombok.Data;

@Data
public class FilterCriteria {
    private String key;
    private Object value;
    private FilterOperation operation;

    public FilterCriteria(String key, Object value, FilterOperation operation) {
        this.key = key;
        this.value = value;
        this.operation = operation;
    }

    // Getters and setters
}
