package ru.job4j.site.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import ru.job4j.site.dto.InterviewDTO;
import ru.job4j.site.dto.ProfileDTO;
import ru.job4j.site.util.RestPageImpl;

import java.util.List;
import java.util.Optional;

@Service
public class InterviewsService {

    private final ProfilesService profilesService;
    private final String mockUrl;

    public InterviewsService(@Value("${service.mock}") String mockUrl, ProfilesService profilesService) {
        this.mockUrl = mockUrl;
        this.profilesService = profilesService;
    }

    public Page<InterviewDTO> getAll(String token, int page, int size)
            throws JsonProcessingException {
        var text = new RestAuthCall(String
                .format("%s/interviews/?page=%d&?size=%d", mockUrl, page, size))
                .get(token);
        var mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        var pageType = mapper.getTypeFactory()
                .constructParametricType(RestPageImpl.class, InterviewDTO.class);
        return mapper.readValue(text, pageType);
    }

    public List<InterviewDTO> getByType(int type) throws JsonProcessingException {
        var text = new RestAuthCall(String.format("%s/interviews/%d", mockUrl, type))
                .get();
        var mapper = new ObjectMapper();
        List<InterviewDTO> interviews =  mapper.readValue(text, new TypeReference<>() { });
        interviews.forEach(interview -> {
            Optional<ProfileDTO> profile = profilesService.getProfileById(interview.getSubmitterId());
            profile.ifPresent(p -> interview.setSubmitterName(p.getUsername()));
        });
        return interviews;
    }

    public Page<InterviewDTO> getByTopicId(int topicId, int page, int size)
            throws JsonProcessingException {
        var text =
                new RestAuthCall(String
                        .format("%s/interviews/findByTopicId/%d?page=%d&?size=%d",
                                mockUrl, topicId, page, size)).get();
        var mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        var pageType = mapper.getTypeFactory()
                .constructParametricType(RestPageImpl.class, InterviewDTO.class);
        return mapper.readValue(text, pageType);
    }

    public Page<InterviewDTO> getByTopicsIds(List<Integer> topicIds, int page, int size)
            throws JsonProcessingException {
        var tids = parseIdsListToString(topicIds);
        var mapper = new ObjectMapper();
        var text =
                new RestAuthCall(String
                        .format("%s/interviews/findByTopicsIds/%s?page=%d&?size=%d",
                                mockUrl, tids, page, size)).get();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        var pageType = mapper.getTypeFactory()
                .constructParametricType(RestPageImpl.class, InterviewDTO.class);
        return mapper.readValue(text, pageType);
    }

    private String parseIdsListToString(List<Integer> list) {
        var builder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            builder.append(list.get(i));
            if (i < list.size() - 1) {
                builder.append(',');
            }
        }
        return builder.toString();
    }
}
