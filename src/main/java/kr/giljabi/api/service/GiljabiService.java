package kr.giljabi.api.service;

import kr.giljabi.api.repository.GiljabiRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GiljabiService {
    private final GiljabiRepository giljabiRepository;

    @Autowired
    public GiljabiService(GiljabiRepository giljabiRepository) {
        this.giljabiRepository = giljabiRepository;
    }
}
