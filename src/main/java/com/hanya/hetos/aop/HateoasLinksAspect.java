package com.hanya.hetos.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@Aspect
@Component
public class HateoasLinksAspect {

    @Around(value = "@annotation(addHateoasLinks)")
    public Object addHateoasLinks(ProceedingJoinPoint joinPoint, AddHateoasLinks addHateoasLinks) throws Throwable {


        Object retVal = joinPoint.proceed();

        if (retVal instanceof ResponseEntity) {
            ResponseEntity<?> responseEntity = (ResponseEntity<?>) retVal;
            Object body = responseEntity.getBody();

            if (!(body instanceof RepresentationModel)) {
                EntityModel<Object> model = createEntityModel(body, addHateoasLinks);
                return new ResponseEntity<>(model, responseEntity.getHeaders(), responseEntity.getStatusCode());
            }
        } else if (!(retVal instanceof RepresentationModel)) {
            EntityModel<Object> model = createEntityModel(retVal, addHateoasLinks);
            return model;
        }
        return retVal;
    }

    private EntityModel<Object> createEntityModel(Object obj, AddHateoasLinks addHateoasLinks) throws NoSuchMethodException {
        Class<?> controller = addHateoasLinks.controller();
        String methodName = addHateoasLinks.method();
        Method targetMethod = findMethod(controller, methodName);

        String fullPath = extractPathFromControllerAndMethod(controller, targetMethod);

        EntityModel<Object> model = EntityModel.of(obj);
        model.add(Link.of(fullPath).withSelfRel());

        return model;
    }

    private Method findMethod(Class<?> controller, String methodName) {
        // 모든 메소드를 순회하면서 이름과 파라미터 유형이 일치하는 메소드 찾기
        // 이 예제에서는 오버로딩을 고려하지 않고, 단순화를 위해 이름만으로 메소드를 찾습니다.
        for (Method method : controller.getDeclaredMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    private String extractPathFromControllerAndMethod(Class<?> controller, Method method) {
        // 컨트롤러와 메소드에 선언된 @RequestMapping 또는 @GetMapping 등의 어노테이션으로부터 경로 추출
        // 복수의 경로가 지정될 수 있으므로, 여기서는 단순화를 위해 첫 번째 경로만을 사용합니다.
        RequestMapping controllerMapping = controller.getAnnotation(RequestMapping.class);
        String basePath = (controllerMapping != null && controllerMapping.value().length > 0) ? controllerMapping.value()[0] : "";

        RequestMapping methodMapping = method.getAnnotation(RequestMapping.class);
        GetMapping getMapping = method.getAnnotation(GetMapping.class);
        PostMapping postMapping = method.getAnnotation(PostMapping.class);
        DeleteMapping deleteMapping = method.getAnnotation(DeleteMapping.class);
        PatchMapping patchMapping = method.getAnnotation(PatchMapping.class);
        PutMapping putMapping = method.getAnnotation(PutMapping.class);

        String methodPath = "";
        if (methodMapping != null && methodMapping.value().length > 0) {
            methodPath = methodMapping.value()[0];
        } else if (getMapping != null && getMapping.value().length > 0) {
            methodPath = getMapping.value()[0];
        } else if (postMapping != null && postMapping.value().length > 0) {
            methodPath = postMapping.value()[0];
        } else if (deleteMapping != null && deleteMapping.value().length > 0) {
            methodPath = deleteMapping.value()[0];
        } else if (patchMapping != null && patchMapping.value().length > 0) {
            methodPath = patchMapping.value()[0];
        } else if (putMapping != null && putMapping.value().length > 0) {
            methodPath = putMapping.value()[0];
        }

        return basePath + methodPath;
    }
}
