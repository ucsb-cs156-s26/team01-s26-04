package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class UCSBOrganizationControllerTests extends ControllerTestCase {

  @MockitoBean UCSBOrganizationRepository ucsbOrganizationRepository;

  @MockitoBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/UCSBOrganization/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/UCSBOrganization/all")).andExpect(status().is(200));
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/UCSBOrganization/post")
                .param("orgCode", "TESTORG")
                .param("orgTranslationShort", "Test Short")
                .param("orgTranslation", "Test Organization")
                .param("inactive", "false")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/UCSBOrganization/post")
                .param("orgCode", "TESTORG")
                .param("orgTranslationShort", "Test Short")
                .param("orgTranslation", "Test Organization")
                .param("inactive", "false")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_ucsborganizations() throws Exception {

    UCSBOrganization org1 =
        UCSBOrganization.builder()
            .orgCode("ACF")
            .orgTranslationShort("Accounting")
            .orgTranslation("Accounting and Finance")
            .inactive(false)
            .build();

    UCSBOrganization org2 =
        UCSBOrganization.builder()
            .orgCode("ART")
            .orgTranslationShort("Art")
            .orgTranslation("Department of Art")
            .inactive(true)
            .build();

    ArrayList<UCSBOrganization> expectedOrganizations = new ArrayList<>();
    expectedOrganizations.addAll(Arrays.asList(org1, org2));

    when(ucsbOrganizationRepository.findAll()).thenReturn(expectedOrganizations);

    MvcResult response =
        mockMvc.perform(get("/api/UCSBOrganization/all")).andExpect(status().isOk()).andReturn();

    verify(ucsbOrganizationRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedOrganizations);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_organization() throws Exception {

    UCSBOrganization organization =
        UCSBOrganization.builder()
            .orgCode("TESTORG")
            .orgTranslationShort("Test Short")
            .orgTranslation("Test Organization")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.save(eq(organization))).thenReturn(organization);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/UCSBOrganization/post")
                    .param("orgCode", "TESTORG")
                    .param("orgTranslationShort", "Test Short")
                    .param("orgTranslation", "Test Organization")
                    .param("inactive", "true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbOrganizationRepository, times(1)).save(organization);
    String expectedJson = mapper.writeValueAsString(organization);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_an_existing_organization() throws Exception {

    // arrange

    UCSBOrganization organization =
        UCSBOrganization.builder()
            .orgCode("TESTORG")
            .orgTranslationShort("Test Short")
            .orgTranslation("Test Organization")
            .inactive(false)
            .build();

    when(ucsbOrganizationRepository.findById(eq("TESTORG"))).thenReturn(Optional.of(organization));

    // act

    MvcResult response =
        mockMvc
            .perform(delete("/api/UCSBOrganization").param("orgCode", "TESTORG").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(ucsbOrganizationRepository, times(1)).findById("TESTORG");
    verify(ucsbOrganizationRepository, times(1)).delete(any());

    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id TESTORG deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_delete_non_existent_organization_and_gets_right_error_message()
      throws Exception {

    // arrange

    when(ucsbOrganizationRepository.findById(eq("MISSING"))).thenReturn(Optional.empty());

    // act

    MvcResult response =
        mockMvc
            .perform(delete("/api/UCSBOrganization").param("orgCode", "MISSING").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert

    verify(ucsbOrganizationRepository, times(1)).findById("MISSING");
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id MISSING not found", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_organization() throws Exception {

    UCSBOrganization originalOrganization =
        UCSBOrganization.builder()
            .orgCode("TESTORG")
            .orgTranslationShort("Old Short")
            .orgTranslation("Old Organization")
            .inactive(false)
            .build();

    UCSBOrganization editedOrganization =
        UCSBOrganization.builder()
            .orgCode("TESTORG")
            .orgTranslationShort("New Short")
            .orgTranslation("New Organization")
            .inactive(true)
            .build();

    String requestBody = mapper.writeValueAsString(editedOrganization);

    when(ucsbOrganizationRepository.findById(eq("TESTORG")))
        .thenReturn(Optional.of(originalOrganization));

    MvcResult response =
        mockMvc
            .perform(
                put("/api/UCSBOrganization")
                    .param("orgCode", "TESTORG")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbOrganizationRepository, times(1)).findById("TESTORG");
    verify(ucsbOrganizationRepository, times(1)).save(originalOrganization);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_organization_that_does_not_exist() throws Exception {

    UCSBOrganization editedOrganization =
        UCSBOrganization.builder()
            .orgCode("MISSING")
            .orgTranslationShort("Missing Short")
            .orgTranslation("Missing Organization")
            .inactive(true)
            .build();

    String requestBody = mapper.writeValueAsString(editedOrganization);

    when(ucsbOrganizationRepository.findById(eq("MISSING"))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(
                put("/api/UCSBOrganization")
                    .param("orgCode", "MISSING")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(ucsbOrganizationRepository, times(1)).findById("MISSING");
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id MISSING not found", json.get("message"));
  }
}
