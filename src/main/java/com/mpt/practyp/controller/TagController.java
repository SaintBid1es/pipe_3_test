package com.mpt.practyp.controller;

import com.mpt.practyp.model.Tag;
import com.mpt.practyp.service.TagService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tags")
public class TagController {

    @Autowired
    private TagService tagService;

    private static final int PAGE_SIZE = 10;

    @GetMapping("/new")
    public String showForm(Model model) {
        model.addAttribute("tag", new Tag());
        return "tag-form";
    }

    @PostMapping("/save")
    public String saveTag(@Valid @ModelAttribute Tag tag, BindingResult result) {
        if (result.hasErrors()) return "tag-form";
        tagService.create(tag);
        return "redirect:/tags/list";
    }

    @GetMapping("/list")
    public String listTags(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean showDeleted,
            Model model
    ) {
        Sort.Direction direction = dir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(direction, sort));

        Page<Tag> tagPage;
        if (search != null && !search.isEmpty()) {
            tagPage = tagService.search(search, showDeleted, pageable);
        } else {
            tagPage = tagService.findAll(showDeleted, pageable);
        }

        model.addAttribute("tagPage", tagPage);
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("showDeleted", showDeleted);
        return "tags-list";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Tag tag = tagService.findById(id);
        if (tag == null) return "redirect:/tags/list";
        model.addAttribute("tag", tag);
        return "tag-form";
    }

    @PostMapping("/update/{id}")
    public String updateTag(@PathVariable Long id, @Valid @ModelAttribute("tag") Tag tag, BindingResult result) {
        if (result.hasErrors()) return "tag-form";
        tag.setId(id);
        tagService.update(tag);
        return "redirect:/tags/list";
    }

    @PostMapping("/delete")
    public String deleteTag(@RequestParam Long id, @RequestParam(required = false) boolean soft) {
        if (soft) tagService.softDelete(id);
        else tagService.hardDelete(id);
        return "redirect:/tags/list";
    }

    @PostMapping("/restore")
    public String restoreTag(@RequestParam Long id) {
        tagService.restore(id);
        return "redirect:/tags/list?showDeleted=true";
    }
}
