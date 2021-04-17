#5649082071433810691
resource "google_compute_instance" "tf-instance-1" {
  name         = "tf-instance-1"
  #machine_type = "n1-standard-1"
  machine_type = "n1-standard-2"
  zone         = var.zone
  allow_stopping_for_update = true
  boot_disk {
    initialize_params {
      image = "debian-cloud/debian-10"
    }
  }

  network_interface {
 #network = "default" #Commenting for task 6
    network = "terraform-vpc"
    subnetwork = "subnet-01"
  }
}
#2356167161402561283
resource "google_compute_instance" "tf-instance-2" {
  name         = "tf-instance-2"
  #machine_type = "n1-standard-1"
  machine_type = "n1-standard-2"
  zone         = var.zone
  allow_stopping_for_update = true
  boot_disk {
    initialize_params {
      image = "debian-cloud/debian-10"
    }
  }

  network_interface {
 #network = "default" #Commenting for task 6
    network = "terraform-vpc"
    subnetwork = "subnet-02"
  }
}
/* Step 5 requires commentting out this resource created in step 4
resource "google_compute_instance" "tf-instance-3" {
  name         = "tf-instance-3"
  machine_type = "n1-standard-2"
  zone         = var.zone
  allow_stopping_for_update = true
  boot_disk {
    initialize_params {
      image = "debian-cloud/debian-10"
    }
  }

  network_interface {
 network = "default"
  }
}
*/
