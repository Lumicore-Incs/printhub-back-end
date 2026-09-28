package com.selling.util;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
public class MapperService {

  private final ModelMapper modelMapper;

  public MapperService(ModelMapper modelMapper) {
    this.modelMapper = modelMapper;
  }

  public <D, T> D map(T source, Class<D> destinationType) {
    return modelMapper.map(source, destinationType);
  }
}
