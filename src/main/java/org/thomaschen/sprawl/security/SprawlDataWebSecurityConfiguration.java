package org.thomaschen.sprawl.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.thomaschen.sprawl.model.User;
import org.thomaschen.sprawl.repository.UserRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@Configuration
@EnableWebSecurity
public class SprawlDataWebSecurityConfiguration extends WebSecurityConfigurerAdapter {
    private static String REALM="SPRAWL";

    @Autowired
    UserRepository userRepository;

    @Autowired
    public void configureGlobalSecurity(AuthenticationManagerBuilder auth) throws Exception {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            auth.inMemoryAuthentication().withUser(user.getUsername()).password(passwordEncoder().encode(user.getPassword())).roles(Role.USER.getText());
        }

        auth.inMemoryAuthentication().withUser("admin1").password(passwordEncoder().encode("abc123")).roles(Role.ADMIN.getText());

        auth.userDetailsService(inMemoryUserDetailsManager());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .cors()
                    .and()
                .authorizeRequests()
                //.antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .antMatchers("/api/admin/**").hasRole("ADMIN")
                    .anyRequest().authenticated()
                    .and()
                .httpBasic()
                    .realmName(REALM)
                    .authenticationEntryPoint(getBasicAuthEntryPoint())
                    .and()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                .csrf().disable();
    }


    @Bean
    public CustomBasicAuthenticationEntryPoint getBasicAuthEntryPoint(){
        return new CustomBasicAuthenticationEntryPoint();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
        final Properties users = new Properties();
        users.put("admin", "abc123,ADMIN,enabled");
        return new InMemoryUserDetailsManager(users);
    }


    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Collections.unmodifiableList(
                Arrays.asList("*")));
        configuration.setAllowedMethods(Collections.unmodifiableList(
                Arrays.asList("GET","POST","PUT","DELETE")));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(Collections.unmodifiableList(
                Arrays.asList("Authorization", "Cache-Control", "Content-Type")));
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
