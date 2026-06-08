package com.example.bankapp.controller;

import org.springframework.ui.Model;
import com.example.bankapp.model.Account;
import com.example.bankapp.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping
public class BankController {

    @Autowired
    private AccountService accountservice;

    @GetMapping("/dashboard")
    public String DashBoard(Model model){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountservice.findAccountByUsername(username);

        model.addAttribute("account", account);


        return "dashboard";
    }

    @GetMapping("/register")
    public String Register(Model model){
        return "register";
    }

    @PostMapping("/register")
    public String RegisterAccount(@RequestParam String username,@RequestParam String password,Model model){

        try {
            accountservice.RegisterAccount(username, password);
            return "redirect:/login";
        }
        catch(Exception e){
            model.addAttribute("error",e.getMessage());
            return "register";
        }

    }

    @GetMapping("/login")
    public String Login(Model model){
        return "login";

    }
    @PostMapping("/deposit")
    public String deposit(@RequestParam BigDecimal amount){

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account = accountservice.findAccountByUsername(username);
        accountservice.deposit(account,amount);

        return "redirect:/dashboard";


    }
    @PostMapping("/withdraw")
    public String withdraw(@RequestParam BigDecimal amount, Model model, RedirectAttributes redirectAttributes){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account  = accountservice.findAccountByUsername(username);
        try {
            accountservice.withdraw(account, amount);
        }
        catch(Exception e) {

            model.addAttribute("account", account);
            model.addAttribute("error", e.getMessage());
            return "dashboard";
        }
        return "redirect:/dashboard";
    }

    @GetMapping("/transactions")
    public String getTransactionHistory(Model model){

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account account  = accountservice.findAccountByUsername(username);

        accountservice.getTransactionHistory(account);
        model.addAttribute("transactions",accountservice.getTransactionHistory(account));

        return "transactions";
    }
    @PostMapping("/transfer")
    public String TransferAmountToUserName(@RequestParam String toUserName,@RequestParam BigDecimal amount,Model model){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Account fromaccount = accountservice.findAccountByUsername(username);

        try{
            accountservice.TransferAmountToUserName(fromaccount,toUserName,amount);
            System.out.println("Transfer Done");
        }
        catch(Exception e){
            model.addAttribute("error",e.getMessage());
            model.addAttribute("account",fromaccount);
            return "dashboard";

        }
        return "redirect:/dashboard";
    }
}
