// ✅ Add Job
async function addJob(job) {
  const response = await fetch('/api/employer/jobs/add', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify([job]) // backend expects a list of jobs
  });

  if (response.ok) {
      alert("✅ Job posted successfully!");
      document.getElementById("addJobForm").reset();

      // optional: refresh job list dynamically
      loadJobs();
    } else {
      const err = await response.text();
      alert("❌ Failed to post job: " + err);
    }
  
  loadJobs(job.employerId);
  document.getElementById("addJobForm").reset();
}

//Load Jobs
  async function loadJobs(employerId) {
    const response = await fetch(`/api/employer/jobs/my-jobs/${employerId}`);
    const jobs = await response.json();
    const jobList = document.getElementById("jobList");

    if (!jobs || jobs.length === 0) {
      jobList.innerHTML = "<p>No jobs posted yet.</p>";
      return;
    }

  jobList.innerHTML = jobs.map(job => `
    <div class="border-bottom pb-3 mb-3">
      <h5>${job.title}</h5>
      <p>${job.description}</p>
      <p><strong>Location:</strong> ${job.location}</p>
      <p><strong>Salary:</strong> ₹${job.salary}</p>
      <p><strong>Deadline:</strong> ${job.deadline}</p>
     <button class="btn btn-warning btn-sm me-2" 
        onclick="editJob(${job.id}, '${job.title}', '${job.description}', '${job.location}', '${job.salary}', '${job.deadline}', ${job.employerId})">Edit</button>
      <button class="btn btn-danger btn-sm" onclick="deleteJob(${job.id})">Delete</button>
    </div>
  `).join("");
}

// Delete Job
async function deleteJob(jobId) {
    if (!confirm("Are you sure you want to delete this job?")) return;

    try {
      const response = await fetch(`/api/jobs/delete/${jobId}`, {
        method: 'DELETE'
      });

      if (response.ok) {
        alert("🗑️ Job deleted successfully!");
        location.reload(); // refresh page after deletion
      } else {
        const message = await response.text();
        alert("❌ Failed to delete job: " + message);
      }
    } catch (error) {
      console.error(error);
      alert("⚠️ Error: Could not delete job.");
    }
  }

//Edit Job
async function editJob(id, title, description, location, salary, deadline, employerId) {
  const newTitle = prompt("Enter new title:", title);
  const newDescription = prompt("Enter new description:", description);
  const newLocation = prompt("Enter new location:", location);
  const newSalary = prompt("Enter new salary:", salary);
  const newDeadline = prompt("Enter new deadline (YYYY-MM-DD):", deadline);

  if (!newTitle || !newDescription || !newLocation || !newSalary || !newDeadline) return;

  const updatedJob = { 
    title: newTitle, 
    description: newDescription, 
    location: newLocation, 
    salary: newSalary,
    deadline: newDeadline  
  };

  const response = await fetch(`/api/employer/jobs/update/${id}?employerId=${employerId}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(updatedJob)
  });

  if (response.ok) {
    alert("✅ Job Edited successfully!");
    loadJobs(employerId);
  } else {
    alert("❌ Failed to Edited job!");
  }
}

// Handle Add Job Form Submission
document.getElementById("addJobForm")?.addEventListener("submit", async (e) => {
  e.preventDefault(); // prevent the default form submission

  // Create the job object from form inputs
  const job = {
    title: document.getElementById("jobTitle").value,
    description: document.getElementById("jobDescription").value,
    location: document.getElementById("jobLocation").value,
    salary: document.getElementById("jobSalary").value,
    deadline: document.getElementById("jobDeadline").value,
    employerId: document.getElementById("employerId").value
  };

  // Call your addJob function
  await addJob(job);
});



// Intercept all status update forms
document.querySelectorAll('form[action="/employer/application-status"]').forEach(form => {
  form.addEventListener('submit', async (event) => {
    event.preventDefault();

    const applicationId = form.querySelector('input[name="applicationId"]').value;
    const status = form.querySelector('select[name="status"]').value;

    try {
      const response = await fetch(
        `/api/employer/jobs/updateStatus?applicationId=${applicationId}&newStatus=${status}&employerId=${document.getElementById("employerId")?.value}`,
        { method: 'POST' }
      );

      if (response.ok) {
        alert("✅ Application status updated successfully!");
        window.location.reload();
      } else {
        const text = await response.text();
        alert("❌ Failed to update status: " + text);
      }
    } catch (error) {
      alert("⚠️ Error: " + error.message);
    }
  });
});



window.addEventListener("DOMContentLoaded", () => {
  const checkEmployerInterval = setInterval(() => {
    const employerId = document.getElementById("employerId")?.value;

    if (employerId) {
      clearInterval(checkEmployerInterval);
      loadJobs(employerId);
    }
  }, 300); // check every 300ms until employerId is found
});



