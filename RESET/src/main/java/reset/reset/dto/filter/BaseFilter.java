package reset.reset.dto.filter;

import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

@Data
public class BaseFilter {
    private int page = 0;
    private int size = 20;
    private List<SortOrder> sort = new ArrayList<>();
    private Boolean ativo = true;

    public Pageable toPageable() {
        if (sort.isEmpty()) {
            return PageRequest.of(page, size);
        }

        List<Sort.Order> orders = new ArrayList<>();
        for (SortOrder sortOrder : sort) {
            orders.add(new Sort.Order(
                    sortOrder.getDirection() == SortOrder.Direction.ASC ? Sort.Direction.ASC : Sort.Direction.DESC,
                    sortOrder.getField()
            ));
        }
        return PageRequest.of(page, size, Sort.by(orders));
    }

    @Data
    public static class SortOrder {
        private String field;
        private Direction direction = Direction.ASC;

        public enum Direction {
            ASC, DESC
        }
    }
}

