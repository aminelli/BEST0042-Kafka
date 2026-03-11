# Kubernetes Deployment Guide

Guida per il deployment del sistema su Kubernetes.

## Prerequisiti

- Kubernetes cluster funzionante
- kubectl configurato
- Docker registry accessibile dal cluster
- TomTom API key

## Preparazione Immagini

### 1. Build Applicazioni

```bash
# Build Java applications
./build-all.sh
```

### 2. Build e Push Immagini Docker

```bash
# Sostituisci YOUR_REGISTRY con il tuo registry (es. docker.io/username, gcr.io/project-id, etc.)
export REGISTRY=YOUR_REGISTRY

# Build immagini
docker build -t $REGISTRY/producer-tomtom:1.0.0 ./producer-tomtom
docker build -t $REGISTRY/stream-processor:1.0.0 ./stream-processor
docker build -t $REGISTRY/consumer-mongodb:1.0.0 ./consumer-mongodb

# Login al registry (se necessario)
docker login $REGISTRY

# Push immagini
docker push $REGISTRY/producer-tomtom:1.0.0
docker push $REGISTRY/stream-processor:1.0.0
docker push $REGISTRY/consumer-mongodb:1.0.0
```

### 3. Aggiorna Manifest Kubernetes

Modifica i file deployment per usare le tue immagini:

```bash
# kubernetes/08-producer-tomtom.yaml
image: YOUR_REGISTRY/producer-tomtom:1.0.0

# kubernetes/09-stream-processor.yaml
image: YOUR_REGISTRY/stream-processor:1.0.0

# kubernetes/10-consumer-mongodb.yaml
image: YOUR_REGISTRY/consumer-mongodb:1.0.0
```

## Configurazione Secret

### Crea Secret per TomTom API Key

```bash
# Encode API key in base64
echo -n 'YOUR_TOMTOM_API_KEY' | base64

# Output esempio: eW91cl90b210b21fYXBpX2tleQ==
```

Modifica `kubernetes/03-secret.yaml`:

```yaml
data:
  TOMTOM_API_KEY: YOUR_BASE64_ENCODED_KEY
```

## Deployment

### Deploy su Kubernetes

```bash
# Apply tutti i manifest nell'ordine corretto
kubectl apply -f kubernetes/01-namespace.yaml
kubectl apply -f kubernetes/02-configmap.yaml
kubectl apply -f kubernetes/03-secret.yaml
kubectl apply -f kubernetes/04-pvc.yaml
kubectl apply -f kubernetes/05-zookeeper.yaml
kubectl apply -f kubernetes/06-kafka.yaml
kubectl apply -f kubernetes/07-mongodb.yaml

# Attendi che infrastruttura sia ready
kubectl wait --for=condition=ready pod -l app=kafka -n traffic-monitoring --timeout=300s
kubectl wait --for=condition=ready pod -l app=mongodb -n traffic-monitoring --timeout=300s

# Deploy applicazioni
kubectl apply -f kubernetes/08-producer-tomtom.yaml
kubectl apply -f kubernetes/09-stream-processor.yaml
kubectl apply -f kubernetes/10-consumer-mongodb.yaml

# (Opzionale) Deploy HPA
kubectl apply -f kubernetes/11-hpa.yaml
```

### Deploy Rapido (tutti i file insieme)

```bash
kubectl apply -f kubernetes/
```

## Verifica Deployment

### Check Pods

```bash
# Lista tutti i pod
kubectl get pods -n traffic-monitoring

# Watch pod status
kubectl get pods -n traffic-monitoring -w

# Describe pod con problemi
kubectl describe pod <pod-name> -n traffic-monitoring
```

### Check Services

```bash
kubectl get services -n traffic-monitoring
kubectl get pvc -n traffic-monitoring
kubectl get configmap -n traffic-monitoring
kubectl get secret -n traffic-monitoring
```

### Verifica Logs

```bash
# Producer logs
kubectl logs -n traffic-monitoring deployment/producer-tomtom -f

# Stream processor logs
kubectl logs -n traffic-monitoring deployment/stream-processor -f

# Consumer logs
kubectl logs -n traffic-monitoring deployment/consumer-mongodb -f

# Kafka logs
kubectl logs -n traffic-monitoring deployment/kafka -f
```

## Accesso ai Servizi

### Port Forward per Debug

```bash
# MongoDB
kubectl port-forward -n traffic-monitoring svc/mongodb 27017:27017
# Ora puoi connetterti a: mongodb://localhost:27017

# Kafka
kubectl port-forward -n traffic-monitoring svc/kafka 9092:9092
# Broker disponibile su: localhost:9092
```

### Exec in Pod

```bash
# MongoDB shell
kubectl exec -it -n traffic-monitoring deployment/mongodb -- mongosh

# Kafka tools
kubectl exec -it -n traffic-monitoring deployment/kafka -- bash
```

## Monitoring

### HorizontalPodAutoscaler Status

```bash
kubectl get hpa -n traffic-monitoring
kubectl describe hpa producer-tomtom-hpa -n traffic-monitoring
```

### Resource Usage

```bash
# Top pods
kubectl top pods -n traffic-monitoring

# Top nodes
kubectl top nodes
```

## Scaling

### Manual Scaling

```bash
# Scale producer
kubectl scale deployment producer-tomtom --replicas=2 -n traffic-monitoring

# Scale stream processor
kubectl scale deployment stream-processor --replicas=3 -n traffic-monitoring

# Scale consumer
kubectl scale deployment consumer-mongodb --replicas=2 -n traffic-monitoring
```

### Autoscaling

HPA configurato per:
- Producer: 1-3 replicas
- Stream Processor: 1-5 replicas
- Consumer: 1-3 replicas

Trigger: CPU >70% o Memory >80%

## Update Applicazioni

### Rolling Update

```bash
# Update immagine
kubectl set image deployment/producer-tomtom \
  producer-tomtom=YOUR_REGISTRY/producer-tomtom:1.1.0 \
  -n traffic-monitoring

# Verifica rollout
kubectl rollout status deployment/producer-tomtom -n traffic-monitoring

# Rollback se necessario
kubectl rollout undo deployment/producer-tomtom -n traffic-monitoring
```

## Troubleshooting

### Pod in CrashLoopBackOff

```bash
# Check logs
kubectl logs <pod-name> -n traffic-monitoring --previous

# Check events
kubectl get events -n traffic-monitoring --sort-by='.lastTimestamp'

# Describe pod
kubectl describe pod <pod-name> -n traffic-monitoring
```

### Problemi di Storage

```bash
# Check PVC status
kubectl get pvc -n traffic-monitoring

# Describe PVC
kubectl describe pvc kafka-data-pvc -n traffic-monitoring
```

### Network Issues

```bash
# Test connettività Kafka
kubectl run -it --rm debug --image=busybox --restart=Never -n traffic-monitoring -- \
  nc -zv kafka 9092

# Test connettività MongoDB
kubectl run -it --rm debug --image=busybox --restart=Never -n traffic-monitoring -- \
  nc -zv mongodb 27017
```

## Cleanup

### Rimuovi Applicazioni (mantieni dati)

```bash
kubectl delete -f kubernetes/08-producer-tomtom.yaml
kubectl delete -f kubernetes/09-stream-processor.yaml
kubectl delete -f kubernetes/10-consumer-mongodb.yaml
kubectl delete -f kubernetes/11-hpa.yaml
```

### Rimuovi Tutto

```bash
# Rimuovi namespace (cancella TUTTO inclusi i dati)
kubectl delete namespace traffic-monitoring
```

### Rimuovi Solo PVC

```bash
kubectl delete pvc --all -n traffic-monitoring
```

## Produzione Best Practices

### 1. Usa StorageClass Appropriate

Modifica `kubernetes/04-pvc.yaml`:

```yaml
storageClassName: fast-ssd  # Esempio per cloud providers
```

### 2. Configura Resource Requests/Limits

Già configurato nei deployment. Modifica in base al workload:

```yaml
resources:
  requests:
    memory: "1Gi"
    cpu: "500m"
  limits:
    memory: "2Gi"
    cpu: "1000m"
```

### 3. Multi-Zone Deployment

Aggiungi affinity rules per distribuire pod su zone diverse:

```yaml
affinity:
  podAntiAffinity:
    preferredDuringSchedulingIgnoredDuringExecution:
    - weight: 100
      podAffinityTerm:
        labelSelector:
          matchExpressions:
          - key: app
            operator: In
            values:
            - kafka
        topologyKey: topology.kubernetes.io/zone
```

### 4. Backup MongoDB

```bash
# Crea backup
kubectl exec -n traffic-monitoring deployment/mongodb -- \
  mongodump --out /tmp/backup

# Copia backup localmente
kubectl cp traffic-monitoring/<mongodb-pod>:/tmp/backup ./backup
```

### 5. Monitoring e Logging

Considera l'integrazione con:
- Prometheus per metriche
- Grafana per dashboard
- ELK/EFK stack per logging centralizzato

## Riferimenti

- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [kubectl Cheat Sheet](https://kubernetes.io/docs/reference/kubectl/cheatsheet/)
- [Kubernetes Best Practices](https://kubernetes.io/docs/concepts/configuration/overview/)
