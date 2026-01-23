import {
    FaCheck,
    FaTimes,
    FaHandPaper,
    FaHourglassHalf,
    FaQuestion,
    FaExclamationTriangle,
} from "react-icons/fa";


export const RegisteredIcon = (props) => <FaCheck {...props} 
    fill="currentColor"
    stroke="black"
    strokeWidth={20}
    strokeLinejoin="round"
    strokeLinecap="round" />;
export const UnregisteredIcon = (props) => <FaTimes {...props} 
    fill="currentColor"
    stroke="black"
    strokeWidth={20}
    strokeLinejoin="round"
    strokeLinecap="round"/>;
export const ExcusedIcon = (props) => <FaHandPaper {...props} 
    fill="currentColor"
    stroke="black"
    strokeWidth={20}
    strokeLinejoin="round"
    strokeLinecap="round"/>;
export const ReservedIcon = (props) => <FaHourglassHalf {...props} 
    fill="currentColor"
    stroke="black"
    strokeWidth={20}
    strokeLinejoin="round"
    strokeLinecap="round"/>;
export const NoResponseIcon = (props) => <FaQuestion {...props} 
    fill="currentColor"
    stroke="black"
    strokeWidth={20}
    strokeLinejoin="round"
    strokeLinecap="round"/>;
export const NoExcusedIcon = (props) => <FaExclamationTriangle {...props} 
    fill="currentColor"
    stroke="black"
    strokeWidth={20}
    strokeLinejoin="round"
    strokeLinecap="round"/>;
