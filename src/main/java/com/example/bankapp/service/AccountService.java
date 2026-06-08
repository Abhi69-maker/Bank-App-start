package com.example.bankapp.service;

import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetailsService;
import java.util.Optional;
import com.example.bankapp.model.Account;
import com.example.bankapp.model.Transaction;
import com.example.bankapp.repository.AccountRepository;
import com.example.bankapp.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionRepository transactionRepository;

    public Account findAccountByUsername(String username){
        return accountRepository.findAccountByUsername(username).
                orElseThrow(() -> new RuntimeException("Account Not Found"));


    }

    public Account RegisterAccount(String username,String password) {
        if (accountRepository.findAccountByUsername(username).isPresent()) {
            throw new RuntimeException("User Already Exists");

        }

            Account account = Account.builder()
                    .username(username)
                    .balance(BigDecimal.ZERO)
                    .password(passwordEncoder.encode(password))
                    .build();



            return accountRepository.save(account);



    }

    public Transaction deposit(Account account, BigDecimal amount){
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .account(account)
                .amount(amount)
                .type("Deposit")
                .timestamp(LocalDateTime.now())
                .build();

        return transactionRepository.save(transaction);
    }

    public void withdraw(Account account,BigDecimal amount){
        if(account.getBalance().compareTo(amount)< 0){
            throw new RuntimeException("Insufficient Balance");
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .account(account)
                .amount(amount)
                .type("withdraw")
                .timestamp(LocalDateTime.now())
                .build();

        transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactionHistory(Account account){
        return transactionRepository.findByAccountId(account.getId());
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        Account account = findAccountByUsername(username);

        return new User(
                account.getUsername(),
                account.getPassword(),
                authorities()
        );



    }

    public Collection<? extends GrantedAuthority> authorities(){
        return Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"));

    }
    @Transactional
    public void TransferAmountToUserName(Account senderaccount,String toUserName,BigDecimal amount){

        if(senderaccount.getBalance().compareTo(amount)<0){
            throw new RuntimeException("Insufficient Balance");
        }

        Account  reciveraccount = accountRepository.findByUsername(toUserName)
                .orElseThrow(() -> new RuntimeException("Recieptent Account Not found"));

        //deduct
        senderaccount.setBalance(senderaccount.getBalance().subtract(amount));

        accountRepository.save(senderaccount);


        //Add
        reciveraccount.setBalance(reciveraccount.getBalance().add(amount));

        accountRepository.save(reciveraccount);


        //Create Transaction Records
        Transaction debitTransaction = Transaction.builder()
                .account(senderaccount)
                .amount(amount)
                .type("Transfer to : "+ reciveraccount.getUsername())
                .timestamp(LocalDateTime.now())
                .build();

        transactionRepository.save(debitTransaction);

        Transaction creditTransaction = Transaction.builder()
                .account(reciveraccount)
                .amount(amount)
                .type("Recieved from : "+ senderaccount.getUsername())
                .timestamp(LocalDateTime.now())
                .build();

        transactionRepository.save(creditTransaction);




    }




}
