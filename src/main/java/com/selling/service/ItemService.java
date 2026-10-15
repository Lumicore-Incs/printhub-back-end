package com.selling.service;

import com.selling.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto createItem(ItemDto dto);

    List<ItemDto> getAllItems();

    ItemDto getItemById(Integer id);

    ItemDto updateItem(Integer id, ItemDto dto);

    void deleteItem(Integer id);
}

