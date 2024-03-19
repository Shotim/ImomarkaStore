import {useState} from "react";
import Dialog from "@mui/material/Dialog";
import axios from "axios";
import {Button, DialogTitle, FormControl, FormGroup, FormHelperText, FormLabel, Input, TextField} from "@mui/material";

function ApplicationForm({openDialog, onClose}) {
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
    const [errors, setErrors] = useState({});

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
        event.preventDefault();
        let droppedFile = event.dataTransfer.files[0];
        console.log(droppedFile);
        if (droppedFile) {
            let inputFieldId = event.target.id;
            console.log(inputFieldId)
            if (inputFieldId === "mainPurposeFile") {
                setMainPurposeFile(droppedFile);
                setFormData({...formData, mainPurpose: ""});
            } else if (inputFieldId === "vinNumberFile") {
                setVinNumberFile(droppedFile);
                setFormData({...formData, vinNumber: ""});
            }
        }
    }

    const handleCancel = (event) => {
        event.preventDefault();
        const inputFieldId = event.target.id;
        console.log(inputFieldId);
        console.log("Removing file...");
        if (inputFieldId === "mainPurposeFile") {
            console.log("mainPurposeFile: " + mainPurposeFile);
            setMainPurposeFile(null);
        } else if (inputFieldId === "vinNumberFile") {
            console.log("vinNumberFile: " + vinNumberFile);
            setVinNumberFile(null);
        }
    }

    const handleChangeFile = (event) => {
        const file = event.target.files[0];
        console.log(file)
        const inputFieldId = event.target.id;
        console.log(inputFieldId);
        if (inputFieldId === "mainPurposeFile") {
            setMainPurposeFile(file);
            setFormData({...formData, mainPurpose: ""});

        } else if (inputFieldId === "vinNumberFile") {
            setVinNumberFile(file);
            setFormData({...formData, vinNumber: ""});
        }
    }

    const validatePhoneNumber = (phoneNumber) => {
        phoneNumber = phoneNumber.trim();
        if (phoneNumber.startsWith("8")) {
            phoneNumber = phoneNumber.replace("8", "+7");
        }
        const formattedPhoneNumber = phoneNumber.replaceAll("[^+\\d]", "");
        return formattedPhoneNumber.length === 12;
    }

    const validateVinNumber = (vinNumber) => {
        return vinNumber &&
            vinNumber.trim().length === 17 &&
            new RegExp("^[A-HJPR-Z\\d]{3}[A-HJPR-Z\\d]{6}[\\dABCDEFGHJKLMNPRSTUVWXYZ]{8}$")
                .test(vinNumber.trim().toUpperCase());
    }

    const validateForm = () => {
        const errors = {};
        if (!formData.name || formData.name.trim() === '') {
            errors.name = 'Name is required.';
        } else if (formData.name.length > 255) {
            errors.name = 'Name must be less than 255 characters.';
        }

        if (!formData.phoneNumber || formData.phoneNumber.trim() === '') {
            errors.phoneNumber = 'Phone number is required.';
        } else if (!validatePhoneNumber(formData.phoneNumber)) { // Your additional validation
            errors.phoneNumber = 'Invalid phone number format.';
        }

        if (!formData.carDetails || formData.carDetails.trim() === '') {
            errors.carDetails = 'Car details are required.';
        } else if (formData.carDetails.length > 255) {
            errors.carDetails = 'Car details must be less than 255 characters.';
        }

        // Optional field with length constraint: Comment
        if (formData.comment && formData.comment.length > 255) {
            errors.comment = 'Comment must be less than 255 characters.';
        }

        if ((!formData.mainPurpose && !mainPurposeFile) || (formData.mainPurpose && mainPurposeFile)) {
            errors.mainPurpose = 'Main purpose requires either text or a photo.';
        } else if (formData.mainPurpose && formData.mainPurpose.length >= 255) {
            errors.mainPurpose = 'Main purpose text must be less than 255 characters.';
        }

        if ((!formData.vinNumber && !vinNumberFile) || (formData.vinNumber && vinNumberFile)) {
            errors.vinNumber = 'VIN number requires either text or a photo.';
        } else if (formData.vinNumber && formData.vinNumber.length >= 255) {
            errors.vinNumber = 'VIN number text must be less than 255 characters.';
        } else if (formData.vinNumber && !validateVinNumber(formData.vinNumber)) {
            errors.vinNumber = 'VIN number in wrong format.';
        }
        setErrors(errors);
        return errors;
    }

    const [showSuccessfulPopup, setShowSuccessfulPopup] = useState(false);
    const [showErrorPopup, setShowErrorPopup] = useState(false);
    const [responseErrorMessage, setResponseErrorMessage] = useState("");
    const handleSendApplication = async (event) => {
        event.preventDefault();

        const errors = validateForm();
        if (Object.keys(errors).length === 0) {
            const requestFormData = new FormData();
            const payload = JSON.stringify(formData);
            requestFormData.append("text", payload);
            requestFormData.append("vinNumberPhoto", vinNumberFile ? vinNumberFile : new Blob([]));
            requestFormData.append("mainPurposePhoto", mainPurposeFile ? mainPurposeFile : new Blob([]));
            const url = `http://localhost:80/api/application`;
            console.log(url);
            axios.post(url,
                requestFormData, {
                    headers: {
                        "Content-Type": "multipart/form-data"
                    }
                }).then(response => {
                console.log("Successful! " + response);
                setFormData({
                    ...formData, name: '',
                    phoneNumber: '',
                    carDetails: '',
                    vinNumber: '',
                    mainPurpose: '',
                    comment: ''
                })
                setShowSuccessfulPopup(true);
            }).catch(error => {
                console.error("Error! " + error);
                setShowErrorPopup(true);
                setResponseErrorMessage(error.response.data);
            })
            setFormData({
                ...formData, name: '',
                phoneNumber: '',
                carDetails: '',
                vinNumber: '',
                mainPurpose: '',
                comment: ''
            });
            setMainPurposeFile(null);
            setVinNumberFile(null);
            document.getElementById("applicationForm").reset();
        } else {
            console.error(errors);

        }
    }

    const handleSuccessfulPopupClose = () => {
        setShowSuccessfulPopup(false);
    };
    const handleErrorPopupClose = () => {
        setShowErrorPopup(false);
        setResponseErrorMessage("");
    };

    const SuccessfulPopup = ({onClose}) => (
        <div className="popup">
            <p>Your request was successful!</p>
            <button onClick={onClose}>Close</button>
        </div>
    );

    const ErrorPopup = ({onClose}) => (
        <div className="popup">
            <p>{responseErrorMessage}</p>
            <button onClick={onClose}>Close</button>
        </div>
    );

    const cancelMainPurposeFile = () => {
        setMainPurposeFile(null);
    };

    const cancelVinNumberFile = () => {
        setVinNumberFile(null);
    };

    return (
        <div>
            <Dialog open={openDialog}>
                <DialogTitle>Create Application</DialogTitle>

                <FormGroup>
                    <FormControl>
                        <FormLabel label="Create Application"/>
                        <TextField label="Name" type="text" id="name"
                                   placeholder="required and less than 255 chars"
                                   required={true}
                                   value={formData.name}
                                   onChange={handleTextInput}/>
                        {errors.name &&
                            <FormHelperText component="span" error={true}>{errors.name}</FormHelperText>}
                    </FormControl>
                    <br/>
                    <FormControl>
                        <TextField label="PhoneNumber" type="text" id="phoneNumber"
                                   placeholder="required and less than 255 chars"
                                   required={true}
                                   value={formData.phoneNumber}
                                   onChange={handleTextInput}/>
                        {errors.phoneNumber &&
                            <FormHelperText component="span" error={true}>{errors.phoneNumber}</FormHelperText>}
                    </FormControl>
                    <br/>
                    <FormControl>
                        <TextField label="CarDetails" type="text" id="carDetails"
                                   placeholder="required and less than 255 chars"
                                   required={true}
                                   value={formData.carDetails}
                                   onChange={handleTextInput}/>
                        {errors.carDetails &&
                            <FormHelperText component="span" error={true}>{errors.carDetails}</FormHelperText>}
                    </FormControl>
                    <br/>
                    <FormControl>
                        <TextField label="Comment" type="text" id="comment"
                                   placeholder="optional but if exists less than 255 chars"
                                   required={false}
                                   value={formData.comment}
                                   onChange={handleTextInput}/>
                        {errors.comment &&
                            <FormHelperText component="span" error={true}>{errors.comment}</FormHelperText>}
                    </FormControl>
                    <br/>
                    <FormControl>
                        <TextField label="MainPurpose" type="text" id="mainPurpose"
                                   placeholder="insert text(less than 255 chars) or drop photo"
                                   required={false}
                                   value={formData.mainPurpose}
                                   onChange={handleTextInput}
                                   disabled={mainPurposeFile !== null}
                        />
                        {errors.mainPurpose &&
                            <FormHelperText component="span" error={true}>{errors.mainPurpose}</FormHelperText>}
                        <Input type="file" id="mainPurposeFile"
                               required={false}
                               onDragOver={handleFileDragOver}
                               onDragLeave={handleDragLeave}
                               onDrop={handleDrop}
                               onCancel={handleCancel}
                               onChange={handleChangeFile}
                               accept="image/*"
                               value={mainPurposeFile === null ? "" : mainPurposeFile.value}
                               disabled={mainPurposeFile !== null}
                        />
                        {mainPurposeFile !== null &&
                            <Button type="reset" id="cancelMainPurposeFile" value="Cancel file upload"
                                    onClick={cancelMainPurposeFile}>Delete mainPurpose file</Button>}
                    </FormControl>
                    <br/>
                    <FormControl>
                        <TextField label="VinNumber" type="text" id="vinNumber"
                                   placeholder="insert text(less than 255 chars) or drop photo"
                                   required={false}
                                   value={formData.vinNumber}
                                   onChange={handleTextInput}
                                   disabled={vinNumberFile !== null}
                        />
                        {errors.vinNumber &&
                            <FormHelperText component="span" error={true}>{errors.vinNumber}</FormHelperText>}
                        <Input type="file" id="vinNumberFile"
                               required={false}
                               onDragOver={handleFileDragOver}
                               onDragLeave={handleDragLeave}
                               onDrop={handleDrop}
                               onCancel={handleCancel}
                               onChange={handleChangeFile}
                               accept="image/*"
                               value={vinNumberFile === null ? "" : vinNumberFile.value}
                               disabled={vinNumberFile !== null}
                        />
                        {vinNumberFile !== null &&
                            <Button type="reset" id="cancelVinNumberFile" value="Cancel file upload"
                                    onClick={cancelVinNumberFile}>Delete vinNumber file</Button>}
                    </FormControl>
                </FormGroup>
                <br/>
                <Button type="submit" onClick={handleSendApplication}>Submit</Button>
                {showSuccessfulPopup && <SuccessfulPopup onClose={handleSuccessfulPopupClose}/>}
                {showErrorPopup && <ErrorPopup onClose={handleErrorPopupClose}/>}
                <Button type="button" onClick={() => {
                    onClose();
                }}>Back to main menu</Button>
            </Dialog>
        </div>
    );
}

export default ApplicationForm;