# Kubernetes & Envoy Gateway Architecture — Developer Reference

This document describes how Kubernetes, Payflow, Services, Gateway API, and Envoy Gateway are organized in the local development environment.

The goal is to make it clear what belongs to Payflow, what is shared infrastructure, and what should happen when future applications are added.

## 1. High-Level Architecture

The local environment uses Docker Desktop with Kubernetes enabled.

The architecture should be thought of as:

Docker Desktop
└── Kubernetes Cluster
    │
    ├── envoy-gateway-system
    │   └── Envoy Gateway infrastructure
    │
    ├── payflow
    │   ├── Payflow Deployment
    │   ├── Payflow Pod(s)
    │   ├── Payflow Service
    │   ├── PostgreSQL
    │   ├── Redis
    │   ├── Gateway
    │   └── HTTPRoute
    │
    └── future-project
        ├── Deployment
        ├── Pod(s)
        ├── Service
        ├── Gateway
        └── HTTPRoute

Important principle

Envoy Gateway is shared Kubernetes infrastructure. Payflow is only one application using that infrastructure.

Do not treat Envoy Gateway as something that belongs permanently to Payflow.

## 2. What Each Kubernetes Component Does
### Pod

A Pod is where the actual application container runs.

For Payflow:

Payflow Pod
└── Payflow application container
    └── port 8080


Pods are temporary/replaceable.

Do not design the system around a specific Pod name or Pod IP.

For example, this:

payflow-7c8f9d...


may disappear and be replaced by:

payflow-5a2b1c...


The application should instead be accessed through a Kubernetes Service.

## 3. Deployment

A Deployment manages Payflow's Pods.

Conceptually:

Deployment
    │
    ├── Pod
    ├── Pod
    └── Pod


If the desired replica count is:

replicas: 1


Kubernetes attempts to keep one Payflow Pod running.

If the Pod crashes, Kubernetes can create another Pod.

To temporarily stop Payflow:

```bash
kubectl scale deployment payflow -n payflow --replicas=0
```


To start it again:

```bash
kubectl scale deployment payflow -n payflow --replicas=1
```


Check the result:

```bash
kubectl get pods -n payflow
```

## 4. Kubernetes Service

The Service provides a stable network endpoint for the Pods.

Instead of:

Client
  ↓
Pod IP


use:

Client
  ↓
Service
  ↓
### Pod


For Payflow:

payflow Service
      │
      └── Payflow Pod :8080


The Service remains stable even if the underlying Pod is recreated.

Example:

kind: Service
metadata:
  name: payflow
  namespace: payflow


The Service is therefore the normal internal Kubernetes networking endpoint for the application.

## 5. Namespace Isolation

Each application should normally have its own namespace.

Current application:

namespace: payflow


Future application:

namespace: ecommerce


Another future application:

namespace: analytics


For example:

Kubernetes Cluster
│
├── payflow
│   ├── Deployment
│   ├── Pods
│   ├── Service
│   └── Gateway resources
│
├── ecommerce
│   ├── Deployment
│   ├── Pods
│   ├── Service
│   └── Gateway resources
│
└── analytics
    ├── Deployment
    ├── Pods
    ├── Service
    └── Gateway resources


This prevents application resources from becoming unnecessarily mixed together.

## 6. Envoy Gateway

Envoy Gateway is infrastructure for handling external traffic and implementing Gateway API resources.

It should be viewed separately from the Payflow application.

The architecture is:

                    Kubernetes Cluster
                           │
                           │
                 ┌─────────▼─────────┐
                 │  Envoy Gateway    │
                 │    Infrastructure │
                 └─────────┬─────────┘
                           │
                 creates/manages Envoy
                           │
                 ┌─────────▼─────────┐
                 │   Envoy Proxy     │
                 └─────────┬─────────┘
                           │
                       HTTPRoute
                           │
                    ┌──────▼──────┐
                    │   Service   │
                    └──────┬──────┘
                           │
                         Pod(s)


The Envoy Gateway infrastructure itself lives in:

envoy-gateway-system


This namespace is not the Payflow namespace.

## 7. Gateway API

Gateway API provides Kubernetes resources for describing how traffic should enter the cluster.

The main resources relevant here are:

GatewayClass
Gateway
HTTPRoute


Think of them roughly as:

GatewayClass
    ↓
Defines the gateway implementation

Gateway
    ↓
Defines the entry point/listener

HTTPRoute
    ↓
Defines where requests should go

## 8. Payflow Gateway

Payflow currently has its own Gateway resource.

Conceptually:

Payflow namespace
│
├── Gateway
│     name: payflow-gateway
│
└── HTTPRoute
      name: payflow-route


The Gateway can expose a listener such as:

HTTP :80


Traffic can then flow:

Browser / Postman
        │
        │ http://localhost
        ▼
   Envoy Proxy
        │
        ▼
 payflow-gateway
        │
        ▼
  payflow HTTPRoute
        │
        ▼
 payflow Service :8080
        │
        ▼
  Payflow Pod :8080

## 9. allowedRoutes Is Important

The current Gateway configuration uses:

allowedRoutes:
  namespaces:
    from: Same


This is an important isolation mechanism.

It means that the Gateway accepts routes from the same namespace as the Gateway.

If:

Gateway:
namespace: payflow
name: payflow-gateway


then a route in:

namespace: payflow


can attach to it.

A route in:

namespace: ecommerce


does not automatically attach to it.

Therefore:

payflow
│
├── payflow-gateway
└── payflow-route        ✅ allowed

ecommerce
│
└── ecommerce-route      ❌ not automatically attached


This is desirable for application isolation.

## 10. Future Applications

When adding another application, do not modify Payflow's resources unnecessarily.

For example, suppose a future project is called ecommerce.

Create:

namespace: ecommerce


and keep its resources there:

ecommerce
│
├── Deployment
├── Pods
├── Service
├── Gateway
└── HTTPRoute


The desired architecture becomes:

Kubernetes Cluster
│
├── envoy-gateway-system
│   └── Shared Envoy Gateway infrastructure
│
├── payflow
│   ├── Payflow Deployment
│   ├── Payflow Pods
│   ├── Payflow Service
│   ├── payflow-gateway
│   └── payflow-route
│
└── ecommerce
    ├── Ecommerce Deployment
    ├── Ecommerce Pods
    ├── Ecommerce Service
    ├── ecommerce-gateway
    └── ecommerce-route


The applications can use the same Envoy Gateway infrastructure without becoming one application.

## 11. Shared Infrastructure vs Application Resources

This distinction should always be kept in mind.

Shared infrastructure

These are generally cluster-level/shared resources:

Docker Desktop
Kubernetes
Envoy Gateway
GatewayClass


These should not be repeatedly installed/uninstalled for every application.

Payflow-specific resources

These belong to Payflow:

payflow namespace
Payflow Deployment
Payflow Pods
Payflow Service
Payflow Gateway
Payflow HTTPRoute
PostgreSQL
Redis
Payflow ConfigMaps
Payflow Secrets

Future application resources

A future application should have its own resources:

ecommerce namespace
Ecommerce Deployment
Ecommerce Pods
Ecommerce Service
Ecommerce Gateway
Ecommerce HTTPRoute
Ecommerce ConfigMaps
Ecommerce Secrets

## 12. Do Not Reinstall Envoy for Every Project

Avoid this mental model:

Payflow
└── Install Envoy

Ecommerce
└── Install Envoy

Analytics
└── Install Envoy


Instead use:

Kubernetes Cluster
│
└── Envoy Gateway
       │
       ├── Payflow Gateway
       │      └── Payflow HTTPRoute
       │
       ├── Ecommerce Gateway
       │      └── Ecommerce HTTPRoute
       │
       └── Analytics Gateway
              └── Analytics HTTPRoute


Envoy Gateway is shared infrastructure.

## 13. Stopping Payflow

If you only want to stop the Payflow application, scale its Deployment down.

```bash
kubectl scale deployment payflow -n payflow --replicas=0
```


Check:

```bash
kubectl get pods -n payflow
```


The Payflow Pod should disappear.

However, the following can remain running:

Envoy Gateway
Payflow Gateway
Payflow HTTPRoute
Redis
PostgreSQL
Other applications


This is normal.

## 14. Starting Payflow Again

Scale the Deployment back up:

```bash
kubectl scale deployment payflow -n payflow --replicas=1
```


Then check:

```bash
kubectl get pods -n payflow
```


Wait for the Deployment:

```bash
kubectl rollout status deployment/payflow -n payflow
```

## 15. Stopping the Entire Local Kubernetes Environment

If you're finished working and want to stop everything, you can stop Docker Desktop/Kubernetes rather than manually scaling every application and infrastructure component.

Conceptually:

Docker Desktop ON
       │
       ▼
Kubernetes
       │
       ├── Envoy Gateway
       ├── Payflow
       ├── Redis
       ├── PostgreSQL
       └── Other applications


When Docker Desktop/Kubernetes is stopped:

Docker Desktop OFF
       │
       ▼
Kubernetes unavailable
       │
       └── workloads stop running


Starting Docker Desktop/Kubernetes again allows Kubernetes to reconcile the existing resources and recreate the required Pods.

## 16. Port Forwarding vs Gateway

There are two different ways to access Payflow during development.

Method 1 — Port Forward

Example:

```bash
kubectl port-forward -n payflow service/payflow 8080:8080
```


Traffic:

localhost:8080
      │
      ▼
kubectl port-forward
      │
      ▼
payflow Service
      │
      ▼
Payflow Pod


This is useful for direct debugging.

The port-forward command must remain running.

Method 2 — Gateway API

With the Gateway configured:

localhost:80
      │
      ▼
Envoy
      │
      ▼
Gateway
      │
      ▼
HTTPRoute
      │
      ▼
payflow Service
      │
      ▼
Payflow Pod


This is the preferred architecture for testing traffic through the application's actual ingress path.

No kubectl port-forward is required.

## 17. Important Port Distinction

Do not confuse:

localhost:80


with:

localhost:8080


They represent different paths.

Gateway path
localhost:80
→ Envoy
→ Gateway
→ HTTPRoute
→ Service :8080
→ Pod :8080

Port-forward path
localhost:8080
→ kubectl port-forward
→ Service :8080
→ Pod :8080


The application's container can still listen on port 8080 internally while the external Gateway listens on port 80.

## 18. Removing Payflow Gateway Resources

If Payflow is permanently being removed, its Gateway resources can be deleted.

For example:

```bash
kubectl delete -f k8s/gateway/httproute.yaml
kubectl delete -f k8s/gateway/gateway.yaml
kubectl delete -f k8s/gateway/gatewayclass.yaml
```


However, be careful with GatewayClass.

A GatewayClass may be shared infrastructure rather than something that should be deleted with a single application.

Before deleting it, verify whether other applications use it.

## 19. Do Not Uninstall Envoy Just to Stop Payflow

Normally, do not do this:

```bash
helm uninstall eg -n envoy-gateway-system
```


just because Payflow is not being used.

Uninstalling Envoy Gateway removes shared infrastructure that future applications may also need.

If Payflow is temporarily stopped:

Keep Envoy Gateway
Keep Gateway infrastructure
Stop/scale Payflow as required

## 20. Useful Kubernetes Commands
See all namespaces
```bash
kubectl get namespaces
```

See Payflow Pods
```bash
kubectl get pods -n payflow
```

See Payflow Services
```bash
kubectl get services -n payflow
```

See Payflow Deployments
```bash
kubectl get deployments -n payflow
```

See Gateway resources
```bash
kubectl get gateway -A
```

See HTTPRoutes
```bash
kubectl get httproute -A
```

See GatewayClasses
```bash
kubectl get gatewayclass
```

See Envoy Gateway infrastructure
```bash
kubectl get pods -n envoy-gateway-system
```

See everything in Payflow
```bash
kubectl get all -n payflow
```

See everything across namespaces
```bash
kubectl get all -A
```

## 21. Recommended Project Structure

A reasonable structure is:

project/
│
├── README.md
│
├── k8s/
│   │
│   ├── namespace.yaml
│   │
│   ├── app/
│   │   ├── deployment.yaml
│   │   └── service.yaml
│   │
│   ├── gateway/
│   │   ├── gateway.yaml
│   │   └── httproute.yaml
│   │
│   ├── config/
│   │   ├── configmap.yaml
│   │   └── secret.yaml
│   │
│   └── data/
│       ├── postgres.yaml
│       └── redis.yaml
│
└── ...


The exact structure can evolve, but keeping application resources organized makes future maintenance easier.

## 22. Rules to Follow When Adding a New Project

When creating another Kubernetes application, follow these rules:

Create a separate namespace.

Example:

ecommerce


Keep the application's Deployment and Pods in that namespace.

Keep the application's Service in that namespace.

Give the application its own Gateway/HTTPRoute configuration when appropriate.

Do not modify Payflow's HTTPRoute just to add another application.

Do not install another copy of Envoy Gateway just because you're creating another project.

Treat envoy-gateway-system as shared infrastructure.

Use allowedRoutes deliberately to control which namespaces can attach routes to a Gateway.

Avoid relying directly on Pod IPs or Pod names.

Use Services for stable application networking.

## 23. Mental Model

The most important mental model is:

                    Kubernetes Cluster
                           │
                           │
                Shared Infrastructure
                           │
                  ┌────────▼────────┐
                  │  Envoy Gateway  │
                  └────────┬────────┘
                           │
            ┌──────────────┼──────────────┐
            │              │              │
            ▼              ▼              ▼
       Payflow         Ecommerce      Analytics
       namespace       namespace      namespace
            │              │              │
         Gateway        Gateway        Gateway
            │              │              │
        HTTPRoute      HTTPRoute      HTTPRoute
            │              │              │
         Service        Service        Service
            │              │              │
          Pods           Pods           Pods


The key separation is:

Envoy Gateway
      ≠
Payflow


Envoy Gateway is infrastructure.

Payflow is an application.

Payflow uses the infrastructure.

## 24. Quick Decision Guide
"I want to stop Payflow temporarily."

Use:

```bash
kubectl scale deployment payflow -n payflow --replicas=0
```

"I want to start Payflow."

Use:

```bash
kubectl scale deployment payflow -n payflow --replicas=1
```

"I want to debug Payflow directly."

Use port-forwarding:

```bash
kubectl port-forward -n payflow service/payflow 8080:8080
```

"I want to test Payflow through the Gateway."

Use:

localhost:80


and let traffic flow through:

Envoy → Gateway → HTTPRoute → Service → Pod

"I'm done working for the day."

Stop Docker Desktop/Kubernetes rather than manually tearing down every component.

"I'm adding another application."

Create a new namespace and application-specific resources.

Do not reinstall Envoy Gateway.

"I'm deleting Payflow permanently."

Remove Payflow's application resources and its application-specific Gateway/HTTPRoute resources.

Only remove shared Envoy infrastructure if you are certain no other application needs it.

## 25. Final Architecture Principle

The environment should evolve toward:

                   Docker Desktop
                         │
                    Kubernetes
                         │
        ┌────────────────┴────────────────┐
        │                                 │
        ▼                                 ▼
Shared Infrastructure              Applications
        │                                 │
        ▼                                 │
Envoy Gateway                           │
        │                                 │
        ├──────────────┬──────────────────┤
        │              │                  │
        ▼              ▼                  ▼
     Payflow        Ecommerce         Future App
     namespace      namespace         namespace
        │              │                  │
     Gateway        Gateway            Gateway
        │              │                  │
    HTTPRoute      HTTPRoute          HTTPRoute
        │              │                  │
     Service        Service            Service
        │              │                  │
      Pods           Pods               Pods


Core rule:

Applications are isolated by namespace and application-specific Kubernetes resources, while Envoy Gateway remains shared infrastructure.

This allows new projects to be added without accidentally changing or taking over Payflow's routing configuration.
