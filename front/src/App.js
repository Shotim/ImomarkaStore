import {useState} from 'react';

function App() {

}

function ApplicationForm() {
    const [formData, setFormData] = useState({
        name: '',
        phoneNumber: '',
        carDetails: '',
        vinNumber: '',
        mainPurpose: '',
        comment: '',
    });

    const [mainPurposeFile, setMainPurposeFile] = useState(null);
    const [vinNumberFile, setVinNumberFile] = useState(null);

    const handleTextInput = (event) => {
        setFormData({...formData, [event.target.id]: event.target.value});
    }

    const handleFileDragOver = (event) => {
        event.preventDefault();
        event.target.classList.add("drop-zone");
    }

    const handleDragLeave = (event) => {
        event.preventDefault();
        event.target.classList.remove("drop-zone");
    }

    const handleDrop = (event) => {
        console.log(event.dataTransfer.files[0])
        event.preventDefault();
        if (event.target.id === "mainPurpose") {
            setMainPurposeFile(event.dataTransfer.files[0]);
            document.getElementById("mainPurpose").value = "";
            console.log("mainPurposeFile: " + mainPurposeFile);
        }
        if (event.target.id === "vinNumber") {
            setVinNumberFile(event.dataTransfer.files[0]);
            document.getElementById("vinNumber").value = "";
            console.log("vinNumberFile: " + vinNumberFile);
        }
    }

    const handleSendApplication = async (event) => {

    }

    return (
        <form id="applicationForm" onSubmit={handleSendApplication}>
            <label htmlFor="name">Name: </label>
            <input type="text" id="name"
                   placeholder="name"
                   value={formData.name}
                   onChange={handleTextInput}/>
            <br/>
            <label htmlFor="phoneNumber">Phone Number: </label>
            <input type="text" id="phoneNumber"
                   placeholder="phone number"
                   value={formData.phoneNumber}
                   onChange={handleTextInput}/>
            <br/>
            <label htmlFor="carDetails">Car Details: </label>
            <input type="text" id="carDetails"
                   placeholder="car details"
                   value={formData.carDetails}
                   onChange={handleTextInput}/>
            <br/>
            <label htmlFor="comment">Comment: </label>
            <input type="text" id="comment"
                   placeholder="comment"
                   value={formData.comment}
                   onChange={handleTextInput}/>
            <br/>
            <label htmlFor="mainPurpose">Main Purpose: </label>
            <input type="text" id="mainPurpose"
                   placeholder="insert text or drop photo"
                   onChange={handleTextInput}
                   disabled={mainPurposeFile !== null}
            />
            <input type="file" id="mainPurposeFile"
                   onDragOver={handleFileDragOver}
                   onDragLeave={handleDragLeave}
                   onDrop={handleDrop}
                   accept="image/*"
            />
            <br/>
            <label htmlFor="vinNumber">VIN number: </label>
            <input type="text" id="vinNumber"
                   placeholder="insert text or drop photo"
                   onChange={handleTextInput}
                   disabled={vinNumberFile !== null}
            />
            <input type="file" id="vinNumberFile"
                   onDragOver={handleFileDragOver}
                   onDragLeave={handleDragLeave}
                   onDrop={handleDrop}
                   accept="image/*"
            />
            <br/>
            <button type="submit">Submit</button>
        </form>
    );
}

export default ApplicationForm;
