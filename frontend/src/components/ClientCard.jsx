import { Link } from 'react-router-dom';

export default function ClientCard({ client }) {
  return (
    <Link
      to={`/clients/${client.id}`}
      className="block rounded-lg border border-gray-200 bg-white p-5 shadow-sm transition hover:shadow-md"
    >
      <div className="flex items-start justify-between gap-3">
        <div>
          <h3 className="text-base font-semibold text-gray-900">{client.name}</h3>
          <p className="mt-1 text-sm text-gray-500">{client.industry || 'No industry set'}</p>
        </div>
        <span className="shrink-0 rounded-full bg-gray-100 px-2.5 py-1 text-xs font-medium text-gray-700">
          {client.healthScore != null ? `Health: ${client.healthScore} ${client.healthEmoji}` : 'Health: —'}
        </span>
      </div>
      <div className="mt-4 flex items-center justify-between text-sm text-gray-500">
        <span>{client.plan}</span>
        <span>
          {client.openIssuesCount ?? 0} open issue{client.openIssuesCount === 1 ? '' : 's'}
        </span>
      </div>
    </Link>
  );
}
