# DigitalOcean View Dashboard
 
 An Spring Boot application that provides a readonly dashboard to list droplets 
 provisioned on a DigitalOcean account.
  

# Deploy On OpenShift

```
oc new-app redhat-openjdk18-openshift:1.2~https://github.com/siamaksade/digitalocean-dashboard --name=digitalocean
oc set env dc/digitalocean DIGITALOCEAN_API_TOKEN=[YOUR-API-TOKEN]

```