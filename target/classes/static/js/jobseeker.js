/* ===== JOB SEEKER JAVASCRIPT ===== */

// Thymeleaf-injected variables (ensure these are set in the HTML template)
const userId = /*[[${userId}]]*/ 0;
const userName = '[[${userName}]]';
const userEmail = '[[${userEmail}]]';

let currentJobId = null;

/* ----- Load All Jobs ----- */
async function loadAllJobs() {
    try {
        const response = await fetch('/api/jobs/all');
        if (!response.ok) throw new Error('Failed to load jobs');
        const jobs = await response.json();
        displayJobs(jobs);
    } catch (err) {
        console.error("loadAllJobs error:", err);
        alert("Error loading jobs. Please try again.");
    }
}

/* ----- Search Jobs ----- */
async function searchJobs() {
    const keyword = document.getElementById("keyword").value.trim();
    const location = document.getElementById("location").value.trim();
    let url = '/api/jobs/search?';
    if (keyword) url += `keyword=${encodeURIComponent(keyword)}`;
    if (location) url += `${keyword ? '&' : ''}location=${encodeURIComponent(location)}`;
    try {
        const response = await fetch(url);
        if (!response.ok) throw new Error("Failed to fetch jobs");
        const jobs = await response.json();
        displayJobs(jobs);
    } catch (err) {
        console.error("searchJobs error:", err);
        alert("Something went wrong while searching jobs!");
    }
}

/* ----- Display Jobs ----- */
function displayJobs(jobs) {
    const container = document.getElementById("jobContainer");
    if (!container) {
        console.error("jobContainer element not found");
        return;
    }
    container.innerHTML = "";

    if (!Array.isArray(jobs) || jobs.length === 0) {
        container.innerHTML = "<p style='text-align:center;'>No jobs found.</p>";
        return;
    }

    jobs.forEach(job => {
        const card = document.createElement("div");
        card.classList.add("job-card");
        card.innerHTML = `
            <h3>${escapeHtml(job.title || 'Untitled Job')}</h3>
            <p>${formatDescription(job.description || '')}</p>
            <p><b>Location:</b> ${escapeHtml(job.location || 'N/A')}</p>
            <p><b>Salary:</b> ₹${job.salary || 'N/A'}</p>
            <p><b>Deadline:</b> ${escapeHtml(job.deadline || 'N/A')}</p>
            <button onclick="openModal(${job.id})">Apply</button>
        `;
        container.appendChild(card);
    });
}

/* ----- Escape HTML to prevent injection ----- */
function escapeHtml(s) {
    if (s == null) return '';
    return String(s)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

/* ----- Format job description with bullet points ----- */
function formatDescription(desc) {
    if (!desc) return '';
    const lines = escapeHtml(desc).split('\n');
    const hasBullet = lines.some(line => line.startsWith("- "));
    if (hasBullet) {
        const items = lines.map(line => line.startsWith("- ") ? `<li>${line.slice(2)}</li>` : `<li>${line}</li>`);
        return `<ul>${items.join('')}</ul>`;
    }
    return escapeHtml(desc).replace(/\n/g, '<br>');
}

/* ----- Open Modal and Load User Details ----- */
async function openModal(jobId) {
    if (!jobId) {
        console.error("Invalid jobId for modal");
        return;
    }
    currentJobId = jobId;
    try {
        const response = await fetch(`/api/user/details?email=${encodeURIComponent(userEmail)}`);
        if (!response.ok) throw new Error('Failed to load user details');
        const data = await response.json();
        document.getElementById('modalName').value = data.name || userName || '';
        document.getElementById('modalEmail').value = data.email || userEmail || '';
        document.getElementById('modalContact').value = data.contact || '';
    } catch (error) {
        console.error("Error loading user details:", error);
        // Fallback to Thymeleaf-injected values
        document.getElementById('modalName').value = userName || '';
        document.getElementById('modalEmail').value = userEmail || '';
        document.getElementById('modalContact').value = '';
    }
    document.getElementById('applyModal').style.display = 'block';
}

/* ----- Close Modal ----- */
function closeModal() {
    document.getElementById('applyModal').style.display = 'none';
    currentJobId = null;  // Reset for safety
}

/* ----- Submit Job Application ----- */
async function submitApplication() {
    const name = document.getElementById('modalName').value.trim();
    const email = document.getElementById('modalEmail').value.trim();
    const contact = document.getElementById('modalContact').value.trim();

    if (!name || !email || !contact) {
        alert("Please fill all fields");
        return;
    }

    if (!currentJobId) {
        alert("No job selected. Please try again.");
        return;
    }

    const payload = {
        jobId: currentJobId,
        userId: userId,
        name: name,
        email: email,
        contactNumber: contact
    };

    try {
        const response = await fetch('/api/applications/apply', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify(payload)
        });

        if (response.ok) {
            closeModal();
            alert("Successfully applied for job!");
            await loadApplicationStatus(); 
            await loadAppliedJobs(); // Refresh status
        } else {
            const errText = await response.text();
            alert("Error applying for job: " + errText);
        }
    } catch (err) {
        console.error("submitApplication error:", err);
        alert("Error applying for job. Check console.");
    }
}

/* ----- Load Application Status ----- */
async function loadApplicationStatus() {
    if (!userId || userId === 0) {
        const list = document.getElementById("statusList");
        if (list) list.innerHTML = "<li>Please log in to see your applications.</li>";
        return;
    }

    try {
        const response = await fetch(`/api/applications/user/${userId}/status`);
        const list = document.getElementById("statusList");
        if (!list) return;
        if (!response.ok) {
            list.innerHTML = "<li>Error loading status</li>";
            return;
        }
        const statuses = await response.json();
        list.innerHTML = "";

        if (!Array.isArray(statuses) || statuses.length === 0) {
            list.innerHTML = "<li>No applications yet.</li>";
            return;
        }

        statuses.forEach(status => {
            const li = document.createElement("li");
            li.textContent = status || 'Unknown Status';

            const s = (status || '').toLowerCase();
            if (s.includes("applied")) li.classList.add("status-applied");
            else if (s.includes("pending")) li.classList.add("status-pending");
            else if (s.includes("shortlisted")) li.classList.add("status-shortlisted");  // Fixed typo: was "Shortlisted"
            else if (s.includes("rejected")) li.classList.add("status-rejected");

            list.appendChild(li);
        });
    } catch (err) {
        console.error("loadApplicationStatus error:", err);
        const list = document.getElementById("statusList");
        if (list) list.innerHTML = "<li>Error loading status</li>";
    }
}

/* ----- Logout ----- */
function logout() {
    // Clear localStorage if used, but rely on server-side session
    localStorage.removeItem("user");
    window.location.href = "/login";
}

/* ----- Initialize on Page Load ----- */
window.onload = () => {
    loadAllJobs();
    loadApplicationStatus();
    loadAppliedJobs();
};


async function loadAppliedJobs() {
  const response = await fetch(`/api/applications/user/${userId}/jobs`);
  const container = document.getElementById("appliedJobsContainer");

  if (!response.ok) {
    container.innerHTML = "<p>Error loading applied jobs</p>";
    return;
  }

  const jobs = await response.json();
  container.innerHTML = jobs.map(job => `
    <div>
      <h4>${job.title}</h4>
      <p>${job.location}</p>
      <p>Status: ${job.status}</p>
    </div>
  `).join("");
}

document.addEventListener("DOMContentLoaded", function() {
  const successMsg = document.querySelector(".alert-success");
  const errorMsg = document.querySelector(".alert-danger");

  if (successMsg || errorMsg) {
    setTimeout(() => {
      if (successMsg) successMsg.style.display = "none";
      if (errorMsg) errorMsg.style.display = "none";
    }, 3000); // hides after 3 seconds
  }
});

  window.addEventListener('DOMContentLoaded', () => {
    const welcome = document.querySelector('.welcome-container');
    if (welcome) {
      setTimeout(() => {
        welcome.style.transition = 'opacity 0.8s ease';
        welcome.style.opacity = '0';
      }, 5000);
    }
  });
