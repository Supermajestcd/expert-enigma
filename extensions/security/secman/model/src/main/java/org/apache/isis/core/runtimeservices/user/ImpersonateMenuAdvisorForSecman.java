package org.apache.isis.core.runtimeservices.user;

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import org.apache.isis.applib.annotation.OrderPrecedence;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.applib.services.user.UserService;
import org.apache.isis.applib.services.user.ImpersonateMenuAdvisor;
import org.apache.isis.extensions.secman.api.role.ApplicationRole;
import org.apache.isis.extensions.secman.api.role.ApplicationRoleRepository;
import org.apache.isis.extensions.secman.api.user.ApplicationUser;
import org.apache.isis.extensions.secman.api.user.ApplicationUserRepository;
import org.apache.isis.extensions.secman.api.user.ApplicationUserStatus;

import lombok.RequiredArgsConstructor;
import lombok.val;

@Service
@Named("isis.runtimeservices.ImpersonateMenuAdvisorDefault")
@Order(OrderPrecedence.MIDPOINT)
@Qualifier("Default")
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ImpersonateMenuAdvisorForSecman implements ImpersonateMenuAdvisor {

    final ApplicationUserRepository<? extends ApplicationUser> applicationUserRepository;
    final ApplicationRoleRepository<? extends ApplicationRole> applicationRoleRepository;

    final UserService userService;
    final MessageService messageService;

    @Override
    public List<String> allUserNames() {
        return this.applicationUserRepository.allUsers()
                .stream()
                .filter(x -> x.getStatus() == ApplicationUserStatus.ENABLED)
                .map(ApplicationUser::getName)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> allRoleNames() {
        return this.applicationRoleRepository.allRoles()
                .stream()
                .map(ApplicationRole::getName)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> roleNamesFor(
            final String username) {
        val applicationUser =
                applicationUserRepository.findByUsername(username)
                        .orElseThrow(RuntimeException::new);
        val applicationRoles = applicationUser.getRoles();
        return applicationRoles
                .stream().map(ApplicationRole::getName)
                .collect(Collectors.toList());
    }

}
