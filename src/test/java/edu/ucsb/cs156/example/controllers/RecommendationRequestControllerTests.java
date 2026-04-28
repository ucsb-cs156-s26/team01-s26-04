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
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {

  @MockitoBean RecommendationRequestRepository recommendationRequestRepository;
  @MockitoBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/recommendationrequest/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/recommendationrequest/all")).andExpect(status().is(200));
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/recommendationrequest/post")
                .param("requesterEmail", "meb@ucsb.edu")
                .param("professorEmail", "pconrad@ucsb.edu")
                .param("explanation", "universityofcalifornia")
                .param("dateRequested", "2022-01-03T00:00:00")
                .param("dateNeeded", "2022-05-01T00:00:00")
                .param("done", "true")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/recommendationrequest/post")
                .param("requesterEmail", "meb@ucsb.edu")
                .param("professorEmail", "pconrad@ucsb.edu")
                .param("explanation", "universityofcalifornia")
                .param("dateRequested", "2022-01-03T00:00:00")
                .param("dateNeeded", "2022-05-01T00:00:00")
                .param("done", "true")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_recommendationrequest() throws Exception {

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2022-05-01T00:00:00");

    RecommendationRequest rr1 = new RecommendationRequest();
    rr1.setRequesterEmail("meb@ucsb.edu");
    rr1.setProfessorEmail("pconrad@ucsb.edu");
    rr1.setExplanation("universityofcalifornia");
    rr1.setDateRequested(ldt1);
    rr1.setDateNeeded(ldt2);
    rr1.setDone(true);

    ArrayList<RecommendationRequest> expectedRecommendationRequest = new ArrayList<>();
    expectedRecommendationRequest.add(rr1);

    when(recommendationRequestRepository.findAll()).thenReturn(expectedRecommendationRequest);

    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequest/all"))
            .andExpect(status().isOk())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedRecommendationRequest);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_recommendationrequest() throws Exception {

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2022-05-01T00:00:00");

    RecommendationRequest rr1 = new RecommendationRequest();
    rr1.setRequesterEmail("meb@ucsb.edu");
    rr1.setProfessorEmail("pconrad@ucsb.edu");
    rr1.setExplanation("universityofcalifornia");
    rr1.setDateRequested(ldt1);
    rr1.setDateNeeded(ldt2);
    rr1.setDone(true);

    when(recommendationRequestRepository.save(eq(rr1))).thenReturn(rr1);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/recommendationrequest/post")
                    .param("requesterEmail", "meb@ucsb.edu")
                    .param("professorEmail", "pconrad@ucsb.edu")
                    .param("explanation", "universityofcalifornia")
                    .param("dateRequested", "2022-01-03T00:00:00")
                    .param("dateNeeded", "2022-05-01T00:00:00")
                    .param("done", "true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).save(eq(rr1));
    String expectedJson = mapper.writeValueAsString(rr1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/recommendationrequest").param("id", "7"))
        .andExpect(status().is(403)); // logged out users can't get by id
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_exist() throws Exception {

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2022-05-01T00:00:00");

    RecommendationRequest rr1 = new RecommendationRequest();
    rr1.setRequesterEmail("meb@ucsb.edu");
    rr1.setProfessorEmail("pconrad@ucsb.edu");
    rr1.setExplanation("universityofcalifornia");
    rr1.setDateRequested(ldt1);
    rr1.setDateNeeded(ldt2);
    rr1.setDone(true);

    when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.of(rr1));

    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequest").param("id", "7"))
            .andExpect(status().isOk())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findById(eq(7L));
    String expectedJson = mapper.writeValueAsString(rr1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    when(recommendationRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(get("/api/recommendationrequest").param("id", "7"))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(recommendationRequestRepository, times(1)).findById(eq(7L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("RecommendationRequest with id 7 not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_recommendationrequest() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2023-01-04T00:00:00");

    RecommendationRequest rr1 = new RecommendationRequest();
    rr1.setRequesterEmail("meb@ucsb.edu");
    rr1.setProfessorEmail("pconrad@ucsb.edu");
    rr1.setExplanation("universityofcalifornia");
    rr1.setDateRequested(ldt1);
    rr1.setDateNeeded(ldt2);
    rr1.setDone(true);

    RecommendationRequest editedrequest = new RecommendationRequest();
    editedrequest.setRequesterEmail("pconrad@ucsb.edu");
    editedrequest.setProfessorEmail("meb@ucsb.edu");
    editedrequest.setExplanation("arizona");
    editedrequest.setDateRequested(ldt2);
    editedrequest.setDateNeeded(ldt1);
    editedrequest.setDone(false);

    String requestBody = mapper.writeValueAsString(editedrequest);

    when(recommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.of(rr1));
    when(recommendationRequestRepository.save(eq(editedrequest))).thenReturn(editedrequest);

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/recommendationrequest")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(67L);
    verify(recommendationRequestRepository, times(1))
        .save(editedrequest); // should be saved with correct user
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_recommendationrequest_that_does_not_exist() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2023-01-04T00:00:00");

    RecommendationRequest rr1 = new RecommendationRequest();
    rr1.setRequesterEmail("meb@ucsb.edu");
    rr1.setProfessorEmail("pconrad@ucsb.edu");
    rr1.setExplanation("universityofcalifornia");
    rr1.setDateRequested(ldt1);
    rr1.setDateNeeded(ldt2);
    rr1.setDone(true);

    String requestBody = mapper.writeValueAsString(rr1);

    when(recommendationRequestRepository.findById(eq(67L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/recommendationrequest")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).findById(67L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("RecommendationRequest with id 67 not found", json.get("message"));
  }
}
