package com.selling.service.impl;

import com.selling.dto.ItemDto;
import com.selling.model.Item;
import com.selling.repository.ItemRepository;
import com.selling.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    private ItemDto toDto(Item item) {
        return new ItemDto(
                item.getProductId(),
                item.getName(),
                item.getPrice(),
                item.getStatus()
        );
    }

    @Override
    public ItemDto createItem(ItemDto dto) {
        Item item = new Item();
        item.setName(dto.getName());
        item.setPrice(dto.getPrice());
        item.setStatus(dto.getStatus());

        return toDto(itemRepository.save(item));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemDto> getAllItems() {
        return itemRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ItemDto getItemById(Integer id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Item not found"));

        return toDto(item);
    }

    @Override
    public ItemDto updateItem(Integer id, ItemDto dto) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Item not found"));

        item.setName(dto.getName());
        item.setPrice(dto.getPrice());
        item.setStatus(dto.getStatus());

        return toDto(itemRepository.save(item));
    }

    @Override
    public void deleteItem(Integer id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Item not found"));

        itemRepository.delete(item);
    }
}

