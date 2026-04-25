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
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBDiningCommonsMenuItemController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonsMenuItemControllerTests extends ControllerTestCase {
  @MockitoBean UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

  @MockitoBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/ucsbdiningcommonsmenuitem/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc
        .perform(get("/api/ucsbdiningcommonsmenuitem/all"))
        .andExpect(status().is(200)); // logged
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/ucsbdiningcommonsmenuitem").param("id", "7"))
        .andExpect(status().is(403)); // logged out users can't get by id
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/ucsbdiningcommonsmenuitem/post")
                .param("diningCommonsCode", "ortega")
                .param("name", "Chicken Caesar Salad")
                .param("station", "Entree Specials")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/ucsbdiningcommonsmenuitem/post")
                .param("diningCommonsCode", "ortega")
                .param("name", "Chicken Caesar Salad")
                .param("station", "Entree Specials")
                .with(csrf()))
        .andExpect(status().is(403)); // only admins can post
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    UCSBDiningCommonsMenuItem ucsbDiningHallMenuItem =
        UCSBDiningCommonsMenuItem.builder()
            .name("Chicken Caesar Salad")
            .diningCommonsCode("ortega")
            .station("Entree Specials")
            .build();

    when(ucsbDiningCommonsMenuItemRepository.findById(eq(7L)))
        .thenReturn(Optional.of(ucsbDiningHallMenuItem));
    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsbdiningcommonsmenuitem").param("id", "7"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq(7L));
    String expectedJson = mapper.writeValueAsString(ucsbDiningHallMenuItem);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_ucsbmenuitem() throws Exception {

    UCSBDiningCommonsMenuItem ucsbDiningHallMenuItem =
        UCSBDiningCommonsMenuItem.builder()
            .name("Chicken Caesar Salad")
            .diningCommonsCode("ortega")
            .station("Entree Specials")
            .build();

    UCSBDiningCommonsMenuItem ucsbDiningHallMenuItem2 =
        UCSBDiningCommonsMenuItem.builder()
            .name("Chicken Caesar Salad")
            .diningCommonsCode("ortega")
            .station("Entrees Specials")
            .build();

    ArrayList<UCSBDiningCommonsMenuItem> expectedMenuItem = new ArrayList<>();
    expectedMenuItem.addAll(Arrays.asList(ucsbDiningHallMenuItem, ucsbDiningHallMenuItem2));

    when(ucsbDiningCommonsMenuItemRepository.findAll()).thenReturn(expectedMenuItem);

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsbdiningcommonsmenuitem/all"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedMenuItem);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_ucsbmenuitem() throws Exception {
    // arrange

    UCSBDiningCommonsMenuItem ucsbDiningHallMenuItem =
        UCSBDiningCommonsMenuItem.builder()
            .name("Chicken Caesar Salad")
            .diningCommonsCode("ortega")
            .station("Entree Specials")
            .build();

    when(ucsbDiningCommonsMenuItemRepository.save(ucsbDiningHallMenuItem))
        .thenReturn(ucsbDiningHallMenuItem);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/ucsbdiningcommonsmenuitem/post")
                    .param("diningCommonsCode", "ortega")
                    .param("name", "Chicken Caesar Salad")
                    .param("station", "Entree Specials")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(ucsbDiningHallMenuItem);
    String expectedJson = mapper.writeValueAsString(ucsbDiningHallMenuItem);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }
}
