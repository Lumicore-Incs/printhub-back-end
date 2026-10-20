package com.selling.service.impl;

import com.selling.dto.StockDto;
import com.selling.model.Item;
import com.selling.model.Stock;
import com.selling.repository.ItemRepository;
import com.selling.repository.StockRepository;
import com.selling.service.StockService;
import com.selling.util.PaginationResponse;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final ItemRepository itemRepository;

    @Override
    public StockDto createStock(StockDto dto) {
        validateStock(dto);
        Item item = findItem(dto.getProductId());
        ensureItemHasNoStock(item.getProductId(), null);

        Stock stock = new Stock();
        copyFields(stock, dto, item);
        return toDto(stockRepository.save(stock));
    }

    @Override
    public StockDto updateStock(Integer id, StockDto dto) {
        validateStock(dto);
        Stock stock = findStock(id);
        Item item = findItem(dto.getProductId());
        ensureItemHasNoStock(item.getProductId(), id);

        copyFields(stock, dto, item);
        return toDto(stockRepository.save(stock));
    }

    @Override
    public void deleteStock(Integer id) {
        stockRepository.delete(findStock(id));
    }

    @Override
    @Transactional(readOnly = true)
    public StockDto getStockById(Integer id) {
        return toDto(findStock(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<StockDto> getStocks(
            int page,
            int size,
            String search,
            String type,
            LocalDate date,
            LocalDate dateFrom,
            LocalDate dateTo,
            String status,
            Integer stockId) {

        validatePage(page, size);
        validateDateFilters(date, dateFrom, dateTo);
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "date")
                        .and(Sort.by(Sort.Direction.DESC, "stockId")));
        Page<Stock> stockPage = stockRepository.findAll(
                buildSpecification(
                        search, type, date, dateFrom, dateTo, status, stockId),
                pageable);

        List<StockDto> content = stockPage.getContent()
                .stream()
                .map(this::toDto)
                .toList();
        return toPaginationResponse(stockPage, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<StockDto> getLowStock(
            int threshold,
            int page,
            int size) {

        validatePage(page, size);
        if (threshold < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "threshold must be greater than or equal to zero");
        }

        Specification<Stock> specification = (root, query, cb) ->
                cb.lessThan(root.<Integer>get("quantity"), threshold);
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.ASC, "quantity")
                        .and(Sort.by(Sort.Direction.DESC, "date")));
        Page<Stock> stockPage =
                stockRepository.findAll(specification, pageable);
        List<StockDto> content = stockPage.getContent()
                .stream()
                .map(this::toDto)
                .toList();
        return toPaginationResponse(stockPage, content);
    }

    private Specification<Stock> buildSpecification(
            String search,
            String type,
            LocalDate date,
            LocalDate dateFrom,
            LocalDate dateTo,
            String status,
            Integer stockId) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (stockId != null) {
                predicates.add(cb.equal(root.get("stockId"), stockId));
            }
            if (hasText(type)) {
                predicates.add(cb.equal(
                        cb.lower(root.get("type")),
                        type.trim().toLowerCase(Locale.ROOT)));
            }
            if (hasText(status)) {
                predicates.add(cb.equal(
                        cb.lower(root.get("status")),
                        status.trim().toLowerCase(Locale.ROOT)));
            }
            if (date != null) {
                predicates.add(cb.equal(root.get("date"), date));
            }
            if (dateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.<LocalDate>get("date"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.<LocalDate>get("date"), dateTo));
            }
            if (hasText(search)) {
                String term = search.trim();
                String keyword = "%" + term.toLowerCase(Locale.ROOT) + "%";
                var item = root.join("item", JoinType.INNER);
                predicates.add(cb.or(
                        cb.like(
                                root.get("stockId").as(String.class),
                                "%" + term + "%"),
                        cb.like(
                                item.get("productId").as(String.class),
                                "%" + term + "%"),
                        cb.like(cb.lower(item.get("name")), keyword)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private void copyFields(Stock stock, StockDto dto, Item item) {
        stock.setType(dto.getType().trim().toLowerCase(Locale.ROOT));
        stock.setQuantity(dto.getQuantity());
        stock.setStatus(normalizeOptional(dto.getStatus()));
        stock.setDate(dto.getDate());
        stock.setItem(item);
    }

    private StockDto toDto(Stock stock) {
        return new StockDto(
                stock.getStockId(),
                stock.getType(),
                stock.getQuantity(),
                stock.getStatus(),
                stock.getDate(),
                stock.getItem().getProductId(),
                stock.getItem().getName());
    }

    private Stock findStock(Integer id) {
        return stockRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Stock not found"));
    }

    private Item findItem(Integer productId) {
        return itemRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Item not found"));
    }

    private void ensureItemHasNoStock(Integer productId, Integer stockId) {
        stockRepository.findByItem_ProductId(productId)
                .filter(existing -> !existing.getStockId().equals(stockId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "A stock record already exists for this product");
                });
    }

    private void validateStock(StockDto dto) {
        if (dto == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Stock request is required");
        }
        if (!hasText(dto.getType())
                || !(dto.getType().equalsIgnoreCase("stock-in")
                || dto.getType().equalsIgnoreCase("stock-out"))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "type must be stock-in or stock-out");
        }
        if (dto.getQuantity() == null || dto.getQuantity() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "quantity must be greater than or equal to zero");
        }
        if (dto.getDate() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "date is required");
        }
        if (dto.getProductId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "productId is required");
        }
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Page must be >= 0 and size must be between 1 and 100");
        }
    }

    private void validateDateFilters(
            LocalDate date, LocalDate dateFrom, LocalDate dateTo) {
        if (date != null && (dateFrom != null || dateTo != null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Use either date or dateFrom/dateTo, not both");
        }
        if (dateFrom != null && dateTo != null
                && dateFrom.isAfter(dateTo)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "dateFrom must be on or before dateTo");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalizeOptional(String value) {
        return hasText(value) ? value.trim() : null;
    }

    private <T> PaginationResponse<T> toPaginationResponse(
            Page<?> page, List<T> content) {
        PaginationResponse<T> response = new PaginationResponse<>();
        response.setContent(content);
        response.setCurrentPage(page.getNumber());
        response.setPageSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        response.setEmpty(page.isEmpty());
        return response;
    }
}
