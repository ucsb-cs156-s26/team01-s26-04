package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "UCSBDiningCommonsMenuItem")
@RequestMapping("/api/ucsbdiningcommonsmenuitem")
@RestController
@Slf4j
public class UCSBDiningCommonsMenuItemController extends ApiController {
  @Autowired UCSBDiningCommonsMenuItemRepository ucsbDiningMenuItems;

  @Operation(summary = "List all ucsb dining hall menu items")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBDiningCommonsMenuItem> allUCSBDiningHallMenuItems() {
    Iterable<UCSBDiningCommonsMenuItem> menuItem = ucsbDiningMenuItems.findAll();
    return menuItem;
  }

  @Operation(summary = "Create a new dining hall menu item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public UCSBDiningCommonsMenuItem postUCSBDiningHallMenuItems(
      @Parameter(name = "diningCommonsCode") @RequestParam String diningCommonsCode,
      @Parameter(name = "name") @RequestParam String name,
      @Parameter(name = "station") @RequestParam String station)
      throws JsonProcessingException {

    UCSBDiningCommonsMenuItem item = new UCSBDiningCommonsMenuItem();
    item.setDiningCommonsCode(diningCommonsCode);
    item.setName(name);
    item.setStation(station);

    UCSBDiningCommonsMenuItem savedMenuItem = ucsbDiningMenuItems.save(item);

    return savedMenuItem;
  }

  @Operation(summary = "Update a single menu item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public UCSBDiningCommonsMenuItem updateUCSBDiningCommonsMenuItem(
      @Parameter(name = "id") @RequestParam Long id,
      @RequestBody @Valid UCSBDiningCommonsMenuItem incoming) {

    UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem =
        ucsbDiningMenuItems
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

    ucsbDiningCommonsMenuItem.setDiningCommonsCode(incoming.getDiningCommonsCode());
    ucsbDiningCommonsMenuItem.setName(incoming.getName()); 
    ucsbDiningCommonsMenuItem.setStation(incoming.getStation());

    ucsbDiningMenuItems.save(ucsbDiningCommonsMenuItem);

    return ucsbDiningCommonsMenuItem;
  }

  @Operation(summary = "Delete a UCSBDiningCommonsMenuItem")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteUCSBMenuItem(@Parameter(name = "id") @RequestParam Long id) {
    UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem =
        ucsbDiningMenuItems
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

    ucsbDiningMenuItems.delete(ucsbDiningCommonsMenuItem);
    return genericMessage("UCSBDiningCommonsMenuItem with id %s deleted".formatted(id));
  }
}
