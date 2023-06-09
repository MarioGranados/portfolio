package com.art.portfolio.Controller;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.art.portfolio.Model.User;
import com.art.portfolio.Repository.PostRepo;
import com.art.portfolio.Repository.UserRepo;
import com.art.portfolio.Service.EmailService;

@Controller
public class UserController {

    private PasswordEncoder passwordEncoder;
    private UserRepo userRepo;
    private PostRepo postRepo;
    private EmailService emailService;

    public UserController(PasswordEncoder passwordEncoder, UserRepo userRepo, PostRepo postRepo, EmailService emailService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.postRepo = postRepo;
        this.emailService = emailService;
    }


    @GetMapping("/sign-up/verify")
    public String showVerificationForm() {
        return "verify";
    }
    @PostMapping("/sign-up/verify")
    public String verify(@RequestParam(name = "verify") String verificationCode) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(user.getCode().equalsIgnoreCase(verificationCode.replaceAll("\\s+",""))) {
            return "redirect:/profile";
        } 
        return "redirect:/verify";
    }

    @GetMapping("/sign-up")
    public String showSignupForm(Model model) {
        model.addAttribute("user", new User());
        return "signup";
    }

    @PostMapping("/sign-up")
    public String saveUser(@ModelAttribute User user, Model model) {
        // Pattern pattern = Pattern.compile(("[^a-zA-Z]"));
        // Matcher matcher = pattern.matcher(user.getPassword());
        // if(matcher.find() && user.getPassword().length() > 8) {
        // System.out.println("user was created");
        // String hash = passwordEncoder.encode(user.getPassword());
        // user.setPassword(hash);
        // userRepo.save(user);
        // } else {
        // System.out.println("password not strong enough");
        // return "redirect:/sign-up";
        // }
        if(userRepo.findByUsername(user.getUsername()) != null || userRepo.findByEmail(user.getEmail()) != null) {
            return "redirect:/sign-up";
        }

        String verificationCode = emailService.generateRandomCode();
        String hash = passwordEncoder.encode(user.getPassword());
        user.setCode(verificationCode);
        emailService.verificationEmail(user, verificationCode);
        user.setPassword(hash);
        userRepo.save(user);
        return "redirect:/login";
    }

    @GetMapping("/profile")
    public String getProfile(Model model) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        model.addAttribute("username", user.getUsername());
        model.addAttribute("post", postRepo.findAllPostsByUserId(user.getId()));
        return "profile";
    }

    @GetMapping("/profile/{username}")
    public String searchByProfileName(@PathVariable String username, Model model) {
        User user = userRepo.findByUsername(username);
        // lets not have any access to passwords through here
        user.setPassword(passwordEncoder.encode("You thought it would be that easy?"));
        model.addAttribute("user", user);
        return "user";
    }

    @GetMapping("/welcome")
    public String onboard() {
        return "Onboard";
    }
}
