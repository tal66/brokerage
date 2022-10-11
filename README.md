### brokerage

<br>

![alt text](demo.JPG "Demo")

```
kubectl apply -f .\deployment.yaml
kubectl apply -f .\service.yaml
kubectl port-forward service/brokerage 8080:8080
curl ...
```