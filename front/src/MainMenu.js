import {useState} from "react";
import ApplicationForm from "./ApplicationForm";

function MainMenu() {

    const [dialog, setDialog] = useState(false);
    const createApplication = () => {
        setDialog(true);
    };
    return <div>
        <h1>Main menu</h1>
        <br/>
        <button type="button" onClick={createApplication}>Create Application</button>
        {dialog === true && <ApplicationForm openDialog={dialog} onClose={() => setDialog(false)}/>}
    </div>
}

export default MainMenu;