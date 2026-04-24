package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {

  @MockitoBean RecommendationRequestRepository recommendationRequestRepository;

  @MockitoBean UserRepository userRepository;

  // Authorization tests for /api/recommendationrequest/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/recommendationrequest/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/recommendationrequest/all")).andExpect(status().is(200));
  }

  // Authorization tests for /api/recommendationrequest/post

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/recommendationrequest/post")
                .param("requesterEmail", "test@ucsb.edu")
                .param("professorEmail", "prof@ucsb.edu")
                .param("explanation", "grad school")
                .param("dateRequested", "2022-01-03T00:00:00")
                .param("dateNeeded", "2022-05-01T00:00:00")
                .param("done", "false")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/recommendationrequest/post")
                .param("requesterEmail", "test@ucsb.edu")
                .param("professorEmail", "prof@ucsb.edu")
                .param("explanation", "grad school")
                .param("dateRequested", "2022-01-03T00:00:00")
                .param("dateNeeded", "2022-05-01T00:00:00")
                .param("done", "false")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  // Tests with mocks for database actions

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_recommendationrequests() throws Exception {

    // arrange
    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2022-05-01T00:00:00");

    RecommendationRequest rr1 =
        RecommendationRequest.builder()
            .requesterEmail("test1@ucsb.edu")
            .professorEmail("prof1@ucsb.edu")
            .explanation("grad school")
            .dateRequested(ldt1)
            .dateNeeded(ldt2)
            .done(false)
            .build();

    RecommendationRequest rr2 =
        RecommendationRequest.builder()
            .requesterEmail("test2@ucsb.edu")
            .professorEmail("prof2@ucsb.edu")
            .explanation("internship")
            .dateRequested(ldt1)
            .dateNeeded(ldt2)
            .done(true)
            .build();

    ArrayList<RecommendationRequest> expectedList = new ArrayList<>();
    expectedList.addAll(Arrays.asList(rr1, rr2));

    when(recommendationRequestRepository.findAll()).thenReturn(expectedList);

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequest/all"))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedList);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_recommendationrequest() throws Exception {

    // arrange
    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2022-05-01T00:00:00");

    RecommendationRequest rr1 =
        RecommendationRequest.builder()
            .requesterEmail("test@ucsb.edu")
            .professorEmail("prof@ucsb.edu")
            .explanation("grad school")
            .dateRequested(ldt1)
            .dateNeeded(ldt2)
            .done(false)
            .build();

    when(recommendationRequestRepository.save(eq(rr1))).thenReturn(rr1);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/recommendationrequest/post")
                    .param("requesterEmail", "test@ucsb.edu")
                    .param("professorEmail", "prof@ucsb.edu")
                    .param("explanation", "grad school")
                    .param("dateRequested", "2022-01-03T00:00:00")
                    .param("dateNeeded", "2022-05-01T00:00:00")
                    .param("done", "false")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).save(rr1);
    String expectedJson = mapper.writeValueAsString(rr1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }
}
